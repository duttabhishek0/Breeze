package com.example.breeze.extras

open class Event <out T>(private val data : T){

    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled() : T?{
        if(hasBeenHandled){
            return  null
        }
        else{
            hasBeenHandled = true;
        }
        return data
    }

    fun peekContent() = data

}