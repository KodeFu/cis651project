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
    .onWrite((change, context) => {
      // Grab the current value of what was written to the Realtime Database.
      const original = change.after.val();

      console.log(`updateSummary groupToken: ${context.params.groupToken}, year: ${context.params.year}, month: ${context.params.month}`);

      return change.after.ref.parent.child('summary/household').set(25);
    });