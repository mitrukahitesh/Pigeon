
var admin = require("firebase-admin");

var serviceAccount = require("H:\\codes\\projects\\PigeonNotif\\nodejs\\private_files\\whatsapp-clone-aa746-firebase-adminsdk-o4z5u-0c94c0af2e.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://whatsapp-clone-aa746.firebaseio.com"
});

var db = admin.database();
var ref = db.ref("NOTIFICATION/");

ref.on("child_added", function(snapshot) {
    console.log(snapshot.val());
    getTokenAndSend(snapshot.val());
    ref.child(snapshot.key).remove();
  }, function (errorObject) {
    console.log("The read failed: " + errorObject.code);
});

function getTokenAndSend(uid) {
    var ref = db.ref(`USERS/${uid}/TOKEN/`);
    ref.once("value", function(snapshot) {
        send(snapshot.val());
    });
}

function send(token) {
    var payload = {
        data: {
            mykey : "Hello"
        }
    };

    var options = {
        priority: "high",
        timeToLive: 60 * 60 * 24
    };

    admin.messaging().sendToDevice(token, payload, options).then(function(response) {
        console.log(response);
    }).catch(function(error) {
        console.log(error)
    });
}
