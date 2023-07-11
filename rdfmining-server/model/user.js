let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let UserSchema = Schema({
    username: String,
    password: String
});

module.exports = mongoose.model('users', UserSchema);