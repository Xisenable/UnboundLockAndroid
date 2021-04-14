package hrw.es.unboundlock

class LoginData {

    // host data
    var prefix: String
    var ip: String
    var port: String
    var host: String

    // Username & Password
    var username: String
    var password: String

    // Lock ID
    var lockID: String

    constructor(prefix: String, ip: String, port: String, host: String, username: String, password: String, lockID: String) {
        this.prefix = prefix
        this.ip = ip
        this.port = port
        this.host = host
        this.username = username
        this.password = password
        this.lockID = lockID
    }




}