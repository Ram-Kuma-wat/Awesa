package com.codersworld.awesalibs.rest

class UniversalObject {
    var response: Any
    var methodName: String
    var status: Boolean
    var msg: String
    var daos: Any? = null

    constructor(result: Any?, methodName: String, status: Boolean, msg: String) {
        if(result == null) {
            this.response = ""
        } else {
            this.response = result
        }

        this.methodName = methodName
        this.status = status
        this.msg = msg
    }

    constructor(result: Any?, methodName: String, status: Boolean, msg: String, daos: Any?) {
        this.daos = daos

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
