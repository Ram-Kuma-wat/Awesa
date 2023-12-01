package com.codersworld.awesalibs.rest



class UniverSelObjct{

    var  response:Any
    var  methodname:String
    var  status:String
    var  msg:String
    var daos1: Any

    constructor(redult: Any, methodname: String , status: String, msg: String) {
        this.daos1 = ""

        if(redult==null){
            this.response = ""
        }else{
            this.response = redult
        }

        this.methodname = methodname
        this.status = status
        this.msg = msg
    }
    constructor(redult: Any, methodname: String , status: String, msg: String, daos: Any) {
        this.daos1 = ""
        if (daos == null) {
            this.daos1 = ""
        } else {
            this.daos1 = daos
        }
        if(redult==null){
            this.response = ""
        }else{
            this.response = redult
        }

        this.methodname = methodname
        this.status = status
        this.msg = msg
    }
}
