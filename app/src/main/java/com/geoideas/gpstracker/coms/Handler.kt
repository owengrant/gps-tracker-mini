package com.geoideas.gpstracker.coms

class Handler {
    var success = { value: Any -> Unit }
    var failure = { value: Any -> Unit }
}
