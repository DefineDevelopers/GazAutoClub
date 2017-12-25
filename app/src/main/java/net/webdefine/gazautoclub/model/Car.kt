package net.webdefine.gazautoclub.model

import java.io.Serializable

data class Car(val id: Int,
               val brandId: Int,
               val name: String,
               val photoUrl: String?): Serializable