package com.iamnippon.bmiandhealth.domain.model

data class GroqResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
