package com.example.myapplication

class User {
    var id : Int = 0
    var username : String = ""
    var email : String = ""
    var password : String = ""

    constructor(username:String, email:String, password:String){
        this.username = username
        this.email = email
        this.password = password
    }
}