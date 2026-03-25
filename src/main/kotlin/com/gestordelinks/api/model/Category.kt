package com.gestordelinks.api.model

import jakarta.persistence.*

@Entity
@Table(name = "categories")

class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false, unique = true, length = 100)
    val name : String = ""
}