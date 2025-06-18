package com.beadpay.samplepos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PayResult(
    val paymentId: String,
    val status: String
) : Parcelable
