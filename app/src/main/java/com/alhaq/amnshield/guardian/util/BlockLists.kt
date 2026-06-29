package com.alhaq.amnshield.guardian.util

object BlockLists {
    val harmfulKeywords: Set<String> = setOf(
        // Keep generic and non-graphic; extend privately as needed
        "adult", "gambling", "bet", "casino", "porn", "nsfw", "xxx"
    )

    val harmfulWebsites: Set<String> = setOf(
        // Examples; replace/extend with your private lists
        "exampleadult.com", "badexample.com"
    )

    val socialMediaDomains: Set<String> = setOf(
        "facebook.com", "instagram.com", "tiktok.com", "twitter.com", "x.com", "snapchat.com", "reddit.com", "pinterest.com"
    )
}

