const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

exports.redeemCoupleInvitation = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Pengguna wajib login terlebih dahulu.");
  }
  const joinerUid = request.auth.uid;
  const rawCode = (request.data.code || "").trim().toUpperCase();
  const joinerNickname = (request.data.nickname || "My Love").trim();

  if (rawCode.length !== 8) {
    throw new HttpsError("invalid-argument", "Kode undangan harus 8 karakter.");
  }

  const serverNow = Date.now();

  return await db.runTransaction(async (tx) => {
    const inviteRef = db.collection("invitationCodes").doc(rawCode);
    const inviteSnap = await tx.get(inviteRef);
    if (!inviteSnap.exists) {
      throw new HttpsError("not-found", "Kode undangan tidak ditemukan.");
    }
    const invite = inviteSnap.data();
    if (invite.isUsed) {
      throw new HttpsError("failed-precondition", "Kode undangan sudah pernah digunakan.");
    }
    if (serverNow > invite.expiresAt) {
      throw new HttpsError("deadline-exceeded", "Kode undangan sudah kedaluwarsa.");
    }
    if (invite.creatorUid === joinerUid) {
      throw new HttpsError("invalid-argument", "Tidak dapat memasangkan akun dengan diri sendiri.");
    }

    const creatorRef = db.collection("users").doc(invite.creatorUid);
    const joinerRef = db.collection("users").doc(joinerUid);
    const coupleRef = db.collection("couples").doc(invite.coupleId);

    const [creatorSnap, joinerSnap, coupleSnap] = await Promise.all([
      tx.get(creatorRef),
      tx.get(joinerRef),
      tx.get(coupleRef),
    ]);

    if (creatorSnap.exists && creatorSnap.data().coupleId) {
      throw new HttpsError("already-exists", "Pembuat kode sudah memiliki pasangan aktif.");
    }
    if (joinerSnap.exists && joinerSnap.data().coupleId) {
      throw new HttpsError("already-exists", "Akun Anda sudah terhubung dengan pasangan.");
    }
    if (coupleSnap.exists && (coupleSnap.data().memberIds || []).length >= 2) {
      throw new HttpsError("resource-exhausted", "Our Space ini sudah penuh (maksimal 2 anggota).");
    }

    const memberIds = [invite.creatorUid, joinerUid];
    const memberNicknames = {
      [invite.creatorUid]: invite.creatorNickname || "Partner",
      [joinerUid]: joinerNickname,
    };

    tx.update(inviteRef, { isUsed: true, usedByUid: joinerUid, usedAt: serverNow });
    tx.set(coupleRef, {
      coupleId: invite.coupleId,
      memberIds,
      memberNicknames,
      anniversaryDateEpochMillis: serverNow,
      tagline: "Two Hearts. One Little Universe.",
      createdAt: serverNow,
      updatedAt: serverNow,
    });
    tx.set(creatorRef, { coupleId: invite.coupleId, partnerUid: joinerUid, updatedAt: serverNow }, { merge: true });
    tx.set(joinerRef, { coupleId: invite.coupleId, partnerUid: invite.creatorUid, updatedAt: serverNow }, { merge: true });

    return { coupleId: invite.coupleId, memberIds, memberNicknames };
  });
});

exports.notifyPartnerOnSharedUpdate = onDocumentWritten(
  "couples/{coupleId}/{subcollection}/{docId}",
  async (event) => {
    const { coupleId, subcollection } = event.params;
    const afterData = event.data && event.data.after ? event.data.after.data() : null;
    if (!afterData) return;

    const coupleSnap = await db.collection("couples").doc(coupleId).get();
    if (!coupleSnap.exists) return;

    const couple = coupleSnap.data();
    const actorUid = afterData.lastEditedByUid || afterData.senderUid || afterData.uploadedByUid || afterData.creatorUid;
    const partnerUid = (couple.memberIds || []).find((uid) => uid !== actorUid);
    if (!partnerUid) return;

    const partnerSnap = await db.collection("users").doc(partnerUid).get();
    const fcmToken = partnerSnap.exists ? partnerSnap.data().fcmToken : null;
    if (!fcmToken) return;

    const eventTypeMap = {
      sharedNotes: "SHARED_NOTE",
      letters: "LOVE_NOTE",
      memories: "NEW_MEMORY",
      events: "CALENDAR_EVENT",
      surprises: "SURPRISE",
    };

    const eventType = eventTypeMap[subcollection] || "GENERAL";
    await admin.messaging().send({
      token: fcmToken,
      data: {
        eventType,
        title: "Our Space 💞",
        body: "Ada pembaruan hangat dari pasanganmu di Our Space.",
      },
    });
  }
);

exports.dispatchScheduledSurprises = onSchedule("every 15 minutes", async () => {
  const serverNow = Date.now();
  const dueSurprises = await db
    .collectionGroup("surprises")
    .where("isRevealed", "==", false)
    .where("scheduledRevealServerMillis", "<=", serverNow)
    .get();

  const batch = db.batch();
  dueSurprises.docs.forEach((doc) => {
    batch.update(doc.ref, { isRevealed: true, revealedAtServerMillis: serverNow });
  });
  await batch.commit();
});
