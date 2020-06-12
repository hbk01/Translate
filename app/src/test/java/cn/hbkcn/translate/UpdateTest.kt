package cn.hbkcn.translate

import cn.hbkcn.translate.update.Response
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author hbk
 * @date 6/10/2020
 * @since 1.0
 */
@RunWith(JUnit4::class)
class UpdateTest {
    @Test
    fun testUpdate() {
        val json =
            "{\"id\":71801,\"tag_name\":\"v2.0.2\",\"target_commitish\":\"df3613e17ad1314e15b237c06fc3f6421ce1b2e2\",\"prerelease\":false,\"name\":\"Release v2.0.2\",\"body\":\"##### 更新内容\\r\\n\\r\\n+ 移除读写存储权限\\r\\n+ 新增错误信息提示\\r\\n+ 新增正在翻译提示\\r\\n+ 编译工具升级\",\"author\":{\"id\":6518576,\"login\":\"hbk01\",\"name\":\"hbk01\",\"avatar_url\":\"https://portrait.gitee.com/uploads/avatars/user/2172/6518576_hbk01_1583322547.jpeg\",\"url\":\"https://gitee.com/api/v5/users/hbk01\",\"html_url\":\"https://gitee.com/hbk01\",\"followers_url\":\"https://gitee.com/api/v5/users/hbk01/followers\",\"following_url\":\"https://gitee.com/api/v5/users/hbk01/following_url{/other_user}\",\"gists_url\":\"https://gitee.com/api/v5/users/hbk01/gists{/gist_id}\",\"starred_url\":\"https://gitee.com/api/v5/users/hbk01/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://gitee.com/api/v5/users/hbk01/subscriptions\",\"organizations_url\":\"https://gitee.com/api/v5/users/hbk01/orgs\",\"repos_url\":\"https://gitee.com/api/v5/users/hbk01/repos\",\"events_url\":\"https://gitee.com/api/v5/users/hbk01/events{/privacy}\",\"received_events_url\":\"https://gitee.com/api/v5/users/hbk01/received_events\",\"type\":\"User\",\"site_admin\":false},\"created_at\":\"2020-06-03T15:59:31+08:00\",\"assets\":[{\"browser_download_url\":\"https://gitee.com/hbk01/Translate/attach_files/406717/download\",\"name\":\"Translate_v2.0.2.apk\"},{\"browser_download_url\":\"https://gitee.com/hbk01/Translate/repository/archive/v2.0.2\"}]}"
        val res = Response(json)
        println("version code: ${res.versionCode()}")
        println("version name: ${res.versionName()}")
        println("apk name: ${res.apkName()}")
        println("apk down url: ${res.apkUrl()}")
        println("release message: ${res.body()}")
        println("is pre release: ${res.preRelease()}")
        println("update time: ${res.updateTime()}")
    }
}