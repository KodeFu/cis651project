const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

// Listens for changes to receipts and updates summary
exports.updateSummary = functions.database.ref('/spending/{groupToken}/receipts/{year}/{month}/detail')
    .onWrite(async (change, context) => {
        console.log(`updateSummary groupToken: ${context.params.groupToken}, year: ${context.params.year}, month: ${context.params.month}`);

        const toWait = [];
        change.after.forEach(function (categorySnapshot) {
            var categoryTotal = 0;
            categorySnapshot.forEach(function (receiptSnapShot) {
                const receipt = receiptSnapShot.val();
                categoryTotal = categoryTotal + parseFloat(receipt.amount);
            });
            toWait.push(change.after.ref.parent.child(`summary/${categorySnapshot.key}`).set(categoryTotal));
        });
        await Promise.all(toWait);

        /* const userTokenRef = admin.database().ref('/userTokens');

        const dataSnapshot = await userTokenRef.once('value');
        const payload = {
            notification: {
                title: 'Receipt Update'
            }
        };

        const toWait = [];
        dataSnapshot.forEach(function (childSnapshot) {
            const deviceToken=childSnapshot.val().toString();
            toWait.push(admin.messaging().sendToDevice(deviceToken, payload));
        });
        await Promise.all(toWait); */
    });
