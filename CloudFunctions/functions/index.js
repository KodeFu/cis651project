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
        // Grab the current value of what was written to the Realtime Database.
        const original = change.after.val();

        console.log(`updateSummary groupToken: ${context.params.groupToken}, year: ${context.params.year}, month: ${context.params.month}`);

        await change.after.ref.parent.child('summary/household').set(25);

        var userTokenRef = admin.database().ref('/userTokens');   
     
        const dataSnapshot = await userTokenRef.once('value');
        const payload = {
            notification: {
                title: 'Receipt Update'
            }
        };

        const toWait = [];
        dataSnapshot.forEach(function (childSnapshot) {
            var deviceToken=childSnapshot.val().toString();
            console.log(childSnapshot.key.toString());    
            toWait.push(admin.messaging().sendToDevice(deviceToken, payload));
        });
        await Promise.all(toWait);
    });
