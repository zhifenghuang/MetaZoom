package com.common.lib.bean

import java.io.Serializable

class VersionBean : Serializable {
    var id: String? = null

    var versionCode: Int = 0

    var versionName: String? = null

    var platform: String? = null

    var link: String? = null

    var title: String? = null

    var status: Int = 0

    var content: String? = null

    var createTime: String? = null
}