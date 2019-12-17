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

        if (!change.after.exists()) {
            console.log(`updateSummary deleting  ${change.after.ref.parent.child('summary').path}`);
            await change.after.ref.parent.child('summary').remove();
            return;
        }

        const currentSummarySnapshot =
            await admin.database().ref(`/spending/${context.params.groupToken}/receipts/${context.params.year}/${context.params.month}/summary`).once('value');

        var databaseUpdates = {};
        var categoriesChanged = {}
        
        change.before.forEach(function (beforeCategorySnapshot) {
            var categoryStillPresent = false;

            change.after.forEach(function (categoryAfterSnapshot) {
                if (beforeCategorySnapshot.key.localeCompare(categoryAfterSnapshot.key) == 0) {
                    categoryStillPresent = true;
                }
            });

            if (!categoryStillPresent) {
                databaseUpdates[`summary/${beforeCategorySnapshot.key}`] = null;
            }
        });

        change.after.forEach(function (categoryAfterSnapshot) {
            var categoryTotal = 0;
            categoryAfterSnapshot.forEach(function (receiptSnapShot) {
                const receipt = receiptSnapShot.val();
                categoryTotal = categoryTotal + parseFloat(receipt.amount);
            });
            const currentValueObject = currentSummarySnapshot.val();
            const currentValue = currentValueObject && currentValueObject[categoryAfterSnapshot.key];
            if (currentValue != categoryTotal) {
                databaseUpdates[`summary/${categoryAfterSnapshot.key}`] = categoryTotal;
                categoriesChanged[`${categoryAfterSnapshot.key}`] = categoryTotal;
            }
        });

        await change.after.ref.parent.update(databaseUpdates);

        const groupsSnapshot =
            await admin.database().ref(`/groups/${context.params.groupToken}`).once('value');

        const userTokensSnapshot =
            await admin.database().ref('/userTokens').once('value');

        const payload = {
            notification: {
                title: '',
                body: ''
            }
        };

        const toWait = [];
        for (var categoryKey of Object.keys(categoriesChanged)) {
            const categoryObject = groupsSnapshot.child(`categories/${categoryKey}`).val();
            if (categoriesChanged[categoryKey] / categoryObject['limit'] > .9) {
                console.log(`Receipt submitted and ${categoryKey} spending now greater than 90% of limit.`);
                payload.notification.title = 'Family Budget'
                payload.notification.body = `Receipt submitted and ${categoryKey} spending now greater than 90% of limit.`

                const membersObject = groupsSnapshot.child('members').val();

                for (var memberKey of Object.keys(membersObject)) {
                    userTokensObject = userTokensSnapshot.val();
                    if (userTokensObject) {
                        for (var userTokenKey of Object.keys(userTokensObject)) {
                            if (memberKey.localeCompare(userTokenKey) == 0) {
                                deviceToken = userTokensObject[userTokenKey];
                                console.log(`Sending notification to deviceToken ${deviceToken}`);
                                toWait.push(admin.messaging().sendToDevice(deviceToken, payload));
                            }
                        }
                    }
                }

            }
        }
        await Promise.all(toWait);
    });
