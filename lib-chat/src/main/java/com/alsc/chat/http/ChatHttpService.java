package com.alsc.chat.http;

import com.common.lib.bean.*;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ChatHttpService {

    /**
     * 修改好友备注/星标
     *
     * @return
     */
    @POST("api/v1/contact/remove")
    Observable<BasicResponse> removeContact(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 修改好友备注/星标
     *
     * @return
     */
    @POST("api/v1/contact/operate")
    Observable<BasicResponse> operateContact(@Query("lang") String lang, @Body HashMap<String, Object> map);

    @Multipart
    @POST("api/v1/contact/list")
    Observable<BasicResponse<ArrayList<UserBean>>> getFriends(@Query("lang") String lang, @Part("currentPage") RequestBody currentPage,
                                                              @Part("pageSize") RequestBody pageSize,
                                                              @Part("star") RequestBody star);

    @Multipart
    @POST("api/v1/contact/list")
    Observable<BasicResponse<ArrayList<UserBean>>> getFriends(@Query("lang") String lang, @Part("currentPage") RequestBody currentPage,
                                                              @Part("pageSize") RequestBody pageSize);

    @Multipart
    @POST("api/v1/contact/list")
    Observable<BasicResponse<ArrayList<UserBean>>> getBlockUsers(@Query("lang") String lang, @Part("currentPage") RequestBody currentPage,
                                                                 @Part("pageSize") RequestBody pageSize,
                                                                 @Part("block") RequestBody block);


    @Multipart
    @POST("api/v1/contact/add")
    Observable<BasicResponse<UserBean>> addContact(@Query("lang") String lang, @Part("contactId") RequestBody contactId,
                                                   @Part("memo") RequestBody memo,
                                                   @Part("remark") RequestBody remark,
                                                   @Part("addType") RequestBody addType);

    @Multipart
    @POST("api/v1/contact/search")
    Observable<BasicResponse<UserBean>> searchContact(@Query("lang") String lang, @Part("mobile") RequestBody mobile);

    @Multipart
    @POST("api/v1/contact/reply")
    Observable<BasicResponse<UserBean>> replayContact(@Query("lang") String lang, @Part("contactId") RequestBody contactId,
                                                      @Part("status") RequestBody status,
                                                      @Part("memo") RequestBody memo);

    @POST("api/v1/contact/review")
    Observable<BasicResponse<ArrayList<UserBean>>> reviewContact(@Query("lang") String lang);

    /*
    退出群
     */
    @Multipart
    @POST("api/v1/group/quit")
    Observable<BasicResponse> exitGroup(@Query("lang") String lang, @Part("groupId") RequestBody groupId);

    /*
    获取群好友列表
     */
    @Multipart
    @POST("api/v1/group/users")
    Observable<BasicResponse<ArrayList<UserBean>>> getGroupUsers(@Query("lang") String lang, @Part("groupId") RequestBody groupId);

    /*
    禁言成员列表
    */
    @Multipart
    @POST("api/v1/group/users")
    Observable<BasicResponse<ArrayList<UserBean>>> getGroupBlockUsers(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                                      @Part("block") RequestBody block);

    /*
解散群组
*/
    @Multipart
    @POST("api/v1/group/dismiss")
    Observable<BasicResponse> dismissGroup(@Query("lang") String lang, @Part("groupId") RequestBody groupId);

    /*
屏蔽词列表
*/
    @Multipart
    @POST("api/v1/groupblock/list")
    Observable<BasicResponse<ArrayList<FilterMsgBean>>> groupBlockList(@Query("lang") String lang, @Part("groupId") RequestBody groupId);

    /*
删除屏蔽词
*/
    @Multipart
    @POST("api/v1/groupblock/delete")
    Observable<BasicResponse> groupBlockDelete(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                               @Part("blockId") RequestBody blockId);

    /*
添加屏蔽词
*/
    @Multipart
    @POST("api/v1/groupblock/create")
    Observable<BasicResponse<FilterMsgBean>> groupBlockCreate(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                              @Part("content") RequestBody content);

    /*
    修改群聊信息
    */
    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse> updateGroup(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                          @Part("name") RequestBody name,
                                          @Part("ownerId") RequestBody ownerId,
                                          @Part("notice") RequestBody notice,
                                          @Part("introduction") RequestBody introduction);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateGroupNotice(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                           @Part("notice") RequestBody notice);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateGroupOwner(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                          @Part("ownerId") RequestBody notice);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateEnterGroupPay(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                             @Part("payinState") RequestBody payinState,
                                                             @Part("payAmount") RequestBody payAmount);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateEnterGroupType(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                              @Part("joinType") RequestBody payinState);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateEnterGroupStint(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                               @Part("joinStint") RequestBody payinState);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateGroupIcon(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                         @Part("icon") RequestBody icon);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> updateGroupName(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                         @Part("name") RequestBody name);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> allBlock(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                  @Part("allBlock") RequestBody allBlock);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> disableFriend(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                       @Part("disableFriend") RequestBody disableFriend);

    @Multipart
    @POST("api/v1/group/update")
    Observable<BasicResponse<GroupBean>> disableLink(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                                     @Part("disableLink") RequestBody disableFriend);

    /*
   修改群成员群内备注
   */
    @Multipart
    @POST("api/v1/group/memo")
    Observable<BasicResponse> updateGroupMemo(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                              @Part("memo") RequestBody memo,
                                              @Part("editUserId") RequestBody editUserId);

    /*
    修改群成员群内备注
   */
    @Multipart
    @POST("api/v1/group/memo")
    Observable<BasicResponse> updateGroupMemo(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                              @Part("memo") RequestBody memo);


    @Multipart
    @POST("api/v1/group/memo")
    Observable<BasicResponse> groupTop(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                       @Part("top") RequestBody top);

    @Multipart
    @POST("api/v1/group/memo")
    Observable<BasicResponse> groupIgnore(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                          @Part("ignore") RequestBody ignore);

    @Multipart
    @POST("api/v1/group/memo")
    Observable<BasicResponse> userTop(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                      @Part("top") RequestBody top);

    @Multipart
    @POST("api/v1/group/memo")
    Observable<BasicResponse> userIgnore(@Query("lang") String lang, @Part("groupId") RequestBody groupId,
                                         @Part("ignore") RequestBody ignore);

    /*
    创建群
    */
    @POST("api/v1/group/create")
    Observable<BasicResponse<GroupBean>> createGroup(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /*
    群信息
    */
    @POST("api/v1/group/overview")
    Observable<BasicResponse<GroupBean>> getGroupInfo(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /*
     我的群组
    */
    @Multipart
    @POST("api/v1/group/mine")
    Observable<BasicResponse<ArrayList<GroupBean>>> getGroups(@Query("lang") String lang, @Part("currentPage") RequestBody currentPage,
                                                              @Part("pageSize") RequestBody pageSize);

    /*
新人红包列表
*/
    @Multipart
    @POST("api/v1/envelope/envelopeList")
    Observable<BasicResponse<ArrayList<EnvelopeBean>>> newcomerList(
            @Query("lang") String lang,
            @Part("groupId") RequestBody groupId,
            @Part("type") RequestBody type);

    /*
     邀请加入群组
    */
    @POST("api/v1/group/invite")
    Observable<BasicResponse> inviteToGroup(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /*
      添加/解除 群成员禁言
    */
    @POST("api/v1/group/groupblock")
    Observable<BasicResponse> addOrRemoveGroupBlock(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /*
    扫码入群
   */
    @POST("api/v1/group/qrcode")
    Observable<BasicResponse<GroupBean>> groupQrcode(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /*
    群主踢人
    */
    @POST("api/v1/group/kickout")
    Observable<BasicResponse> kickout(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 红包/转账详情
     *
     * @return
     */
    @POST("api/v1/envelope/overview")
    Observable<BasicResponse<EnvelopeBean>> envelopeDetail(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 领取红包/转账
     *
     * @return
     */
    @POST("api/v1/envelope/draw")
    Observable<BasicResponse<EnvelopeBean>> envelopeDraw(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 发送红包/转账
     *
     * @return
     */
    @POST("api/v1/envelope/send")
    Observable<BasicResponse<EnvelopeBean>> envelopeSend(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 撤回新人红包
     *
     * @return
     */
    @POST("/api/v1/envelope/unLockNewEnvelope")
    Observable<BasicResponse<EnvelopeBean>> unLockNewEnvelope(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 发出/收到的红包
     *
     * @return
     */
    @POST("api/v1/envelope/list")
    Observable<BasicResponse<EnvelopeRecordBean>> envelopeList(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /**
     * 创建标签
     *
     * @return
     */
    @POST("api/v1/tag/create")
    Observable<BasicResponse> createTag(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 标签列表
     *
     * @return
     */
    @POST("api/v1/tag/list")
    Observable<BasicResponse<ArrayList<LabelBean>>> getTagList(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 标签好友
     *
     * @return
     */
    @POST("api/v1/tag/contacts")
    Observable<BasicResponse<ArrayList<UserBean>>> tagContacts(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 删除标签
     *
     * @return
     */
    @POST("api/v1/tag/remove")
    Observable<BasicResponse> deleteTag(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 创建标签
     *
     * @return
     */
    @POST("api/v1/tag/save")
    Observable<BasicResponse<LabelBean>> editTag(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 修改个人信息
     *
     * @return
     */
    @POST("api/v1/user/save")
    Observable<BasicResponse> updateUserProfile(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 个人信息
     *
     * @return
     */
    @POST("api/v1/user/profile")
    Observable<BasicResponse<UserBean>> getUserProfile(@Query("lang") String lang);

    /**
     * 个人信息
     *
     * @return
     */
    @Multipart
    @POST("api/v1/contact/overview")
    Observable<BasicResponse<UserBean>> getContactProfile(@Query("lang") String lang, @Part("contactId") RequestBody contactId);

    /**
     * 留言
     *
     * @return
     */
    @POST("api/v1/suggest/create")
    Observable<BasicResponse> suggest(@Query("lang") String lang, @Body HashMap<String, Object> map);

    /**
     * 自动回复问题列表
     *
     * @return
     */
    @POST("api/v1/question/list")
    Observable<BasicResponse<ArrayList<QuestionBean>>> questionList(@Query("lang") String lang);

    /**
     * 自动回复问题详情
     *
     * @return
     */
    @POST("api/v1/question/overview")
    Observable<BasicResponse> questionDetail(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /**
     * 在线客服列表
     *
     * @return
     */
    @POST("api/v1/question/online")
    Observable<BasicResponse<ArrayList<UserBean>>> questionOnline(@Query("lang") String lang);


    /**
     * 转账手续费
     *
     * @return
     */
    @POST("api/v1/envelope/transfer")
    Observable<BasicResponse<TransferFeeBean>> transferFee(@Query("lang") String lang);


    /**
     * 推送
     *
     * @return
     */
    @POST("api/v1/user/device")
    Observable<BasicResponse> userDevice(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /**
     * 添加收藏
     *
     * @return
     */
    @POST("api/v1/groupblock/add")
    Observable<BasicResponse> addCollection(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /**
     * 取消收藏
     *
     * @return
     */
    @POST("api/v1/groupblock/del")
    Observable<BasicResponse> delCollection(@Query("lang") String lang, @Body HashMap<String, Object> map);


    /**
     * 收藏列表
     *
     * @return
     */
    @POST("api/v1/groupblock/favorites")
    Observable<BasicResponse<ArrayList<CollectionBean>>> getCollections(@Query("lang") String lang, @Body HashMap<String, Object> map);

}
