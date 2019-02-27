/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.materialdonations.billingx

import android.content.Context
import com.android.billingclient.api.PurchasesUpdatedListener
import com.ivianuu.materialdonations.BillingClientFactory
import com.pixite.android.billingx.DebugBillingClient

/**
 * Debug billing client factory
 */
class DebugBillingClientFactory : BillingClientFactory {
    override fun createBillingClient(
        context: Context,
        listener: PurchasesUpdatedListener
    ): DebugBillingClient {
        return DebugBillingClient.newBuilder(context)
            .setListener(listener)
            .build()
    }
}