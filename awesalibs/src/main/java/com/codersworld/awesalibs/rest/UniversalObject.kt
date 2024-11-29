package com.codersworld.awesalibs.rest

class UniversalObject {

    var response:Any
    var methodName:String
    var status:String
    var msg:String
    var daos: Any

    constructor(result: Any?, methodName: String, status: String, msg: String) {
        this.daos = ""

        if(result == null) {
            this.response = ""
        } else {
            this.response = result
        }

        this.methodName = methodName
        this.status = status
        this.msg = msg
    }
    constructor(result: Any?, methodName: String, status: String, msg: String, daos: Any?) {
        this.daos = ""
        if (daos == null) {
            this.daos = ""
        } else {
            this.daos = daos
        }
        if(result == null) {
            this.response = ""
        } else {
            this.response = result
        }

        this.methodName = methodName
        this.status = status
        this.msg = msg
    }
}
