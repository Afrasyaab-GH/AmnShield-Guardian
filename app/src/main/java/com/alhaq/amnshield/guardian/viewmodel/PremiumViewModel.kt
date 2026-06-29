package com.alhaq.amnshield.guardian.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.alhaq.amnshield.guardian.premium.PremiumManager
import javax.inject.Inject

/**
 * PremiumViewModel - Provides access to PremiumManager
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    val premiumManager: PremiumManager
) : ViewModel()

