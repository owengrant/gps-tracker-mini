package com.geoideas.gpstracker.coms

import org.json.JSONObject

class Parser {

    val P_UNKNOWN = "unknown"
    val P_ERROR = "error"
    val P_CODE = "code"
    val P_OP = "op"

    val P_L = "l"
    val P_G = "g"
    val P_GK = "gk"
    val P_GC = "gc"
    val P_GR = "gr"
    val P_GA = "ga"
    val P_GD = "gd"
    val P_GE = "ge"
    val P_GEK = "gek"
    val P_GEA = "gea"
    val P_T = "t"
    val P_TL = "tl"
    val P_TLN = "tln"
    val P_TB = "tb"
    val P_TA = "ta"
    val P_TLA = "tla"
    val P_TLNA = "tlna"
    val P_TBA = "tba"

    val P_C = "c"
    val P_R = "r"
    val P_TI = "ti"
    val P_K = "k"
    val P_I = "i"
    val P_V = "v"
    val P_S = "s"
    val P_E = "e"
    val P_A = "a"


    open fun parser(mes: String): String {
        if(mes.isBlank()) return P_UNKNOWN
        try {
            val json = JSONObject(mes)
            if(!json.has(P_CODE) || !json.has(P_OP)) return P_ERROR
            val op = json.getString(P_OP)
            return when(op){
                P_L -> P_L
                P_G -> P_G
                P_GK -> P_GK
                P_GC -> P_GC
                P_GR -> P_GR
                P_GA -> P_GA
                P_GD -> P_GD
                P_GE -> P_GE
                P_GEK -> P_GEK
                P_GEA -> P_GEA
                P_T -> P_T
                P_TL -> P_TL
                P_TLN -> P_TLN
                P_TB -> P_TB
                P_TA -> P_TA
                P_TLA -> P_TLA
                P_TLNA -> P_TLNA
                P_TBA -> P_TBA
                else -> P_UNKNOWN
            }
        }
        catch(e: Exception) { return P_UNKNOWN }
    }

}