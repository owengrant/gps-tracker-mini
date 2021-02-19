package com.geoideas.gpstrackermini.coms

class Handler {
    var success = { value: Any -> Unit }
    var failure = { value: Any -> Unit }
}
