package org.alhaq.deenshield.guardian.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.alhaq.deenshield.guardian.premium.PremiumManager
import javax.inject.Inject

/**
 * PremiumViewModel - Provides access to PremiumManager
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    val premiumManager: PremiumManager
) : ViewModel()
