/*
 * (c)2014 COMPUTER SYSTEM Corp. All Rights Reserved
 *
 * 機能名　　　　：DAOクラス
 * ファイル名　　：TblEventDao.java
 * クラス名　　　：TblEventDao
 * 概要　　　　　：tbl_event イベントの開催に関する各種値テーブルのDAOを提供する。
 * バージョン　　：
 *
 * 改版履歴　　　：
 * 2014/09/24 <新規>    新規作成
 *
 */
package jp.co.csc.iberaku.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import jp.co.csc.iberaku.bean.EventBean;
import jp.compsys.common.CompSysException;
import jp.compsys.common.db.DaoPageInfo;
import jp.compsys.common.db.DbBase;
import jp.compsys.common.db.DbI;
import jp.compsys.common.db.DbO;
import jp.compsys.common.db.DbS;
import jp.compsys.common.db.GetNumber;

/**
 * tbl_event イベントの開催に関する各種値テーブルのDAOを提供する。.
 *
 * @author COMPUTER SYSTEM Corp.
 * @version 1.0
 */
public class EventDao
{
	/**
	 * ソートフィールドのチェック時に使う。SQLインジェクション対策用。
	 */
	private static HashMap<String, String> fieldsArray = new HashMap<String, String>();

	/**
	 * 初期化処理。
	 */
	static
	{
		fieldsArray.put("event_id", "tbl_event.event_id");
		fieldsArray.put("user_id", "tbl_event.user_id");
		fieldsArray.put("title", "tbl_event.title");
		fieldsArray.put("event_date", "tbl_event.event_date");
		fieldsArray.put("place", "tbl_event.place");
		fieldsArray.put("start_time", "tbl_event.start_time");
		fieldsArray.put("end_time", "tbl_event.end_time");
		fieldsArray.put("gather_place", "tbl_event.gather_place");
		fieldsArray.put("gather_time", "tbl_event.gather_time");
		fieldsArray.put("answer_time", "tbl_event.answer_time");
		fieldsArray.put("other", "tbl_event.other");
		fieldsArray.put("outline", "tbl_event.outline");
		fieldsArray.put("force_closed", "tbl_event.force_closed");
		fieldsArray.put("update_flgs", "tbl_event.update_flgs");
		fieldsArray.put("delete_flg", "tbl_event.delete_flg");
		fieldsArray.put("regist_time", "tbl_event.regist_time");
		fieldsArray.put("update_time", "tbl_event.update_time");
		fieldsArray.put("member_count", "member.member_count");
		fieldsArray.put("before_send_datetime", "tbl_event.before_send_datetime");
		fieldsArray.put("capacity", "tbl_event.capacity");
		fieldsArray.put("sent_flg", "tbl_event.sent_flg");
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルを検索しtbl_event イベントの開催に関する各種値テーブルの１行を取得します。.
	 *
	 * @param pEventId イベントID
	 * @return 値を格納したEventBeanオブジェクト
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static EventBean dbSelect(String pEventId) throws CompSysException
	{
		String sql = "select "
				
				+ " tbl_event.event_id"
				+ ",tbl_event.user_id"
				+ ",tbl_event.title"
				+ ",tbl_event.event_date"
				+ ",tbl_event.place"
				+ ",tbl_event.start_time"
				+ ",tbl_event.end_time"
				+ ",tbl_event.gather_place"
				+ ",tbl_event.gather_time"
				+ ",tbl_event.answer_time"
				+ ",tbl_event.other"
				+ ",tbl_event.outline"
				+ ",tbl_event.force_closed"
				+ ",tbl_event.regist_time"
				+ ",tbl_event.update_time"
				+ ",tbl_event.update_flgs"
				+ ",tbl_event.delete_flg"
				+ ",tbl_event.before_send_datetime"
				+ ",tbl_event.capacity"
				+ ",tbl_event.sent_flg"
				+ ",tbl_event.is_votable" // ★★★ 追加！ここ！
				+ ",tbl_user.name"
				+ ",COALESCE(attendanceCount.count,0)"
				+ "as attendanceCount"
				+ ",COALESCE(absenceCount.count,0)"
				+ "as absenceCount"
				+ ",COALESCE(reservationCount.count,0)"
				+ "as reservationCount"
				+ " from tbl_event"
				+ " left join"
				+ " tbl_user"
				+ " on tbl_event.user_id = tbl_user.user_id"
				+ " left join"
				+ " ("
				+ " select count(*)"
				+ " as count"
				+ ",event_id"
				+ " from tbl_event_member"
				+ " inner join tbl_member on tbl_event_member.member_id = tbl_member.member_id "
				+ " where tbl_event_member.status='1'";
		sql += " group by tbl_event_member.event_id ) attendanceCount"
				+ " on tbl_event.event_id = attendanceCount.event_id"
				+ " left join"
				+ " ("
				+ " select count(*)"
				+ " as count"
				+ ",event_id"
				+ " from tbl_event_member"
				+ " inner join tbl_member on tbl_event_member.member_id = tbl_member.member_id "
				+ " where tbl_event_member.status='2'";
		sql += " group by tbl_event_member.event_id ) absenceCount"
				+ " on tbl_event.event_id = absenceCount.event_id"
				+ " left join"
				+ " ("
				+ " select count(*)"
				+ " as count"
				+ ",event_id"
				+ " from tbl_event_member"
				+ " inner join tbl_member on tbl_event_member.member_id = tbl_member.member_id "
				+ " where tbl_event_member.status='3'";
		sql += " group by tbl_event_member.event_id ) reservationCount"
				+ " on tbl_event.event_id = reservationCount.event_id"
				+ "";
		sql += ""
				+ " where tbl_event.event_id = " + DbS.character(pEventId);
		List<HashMap<String, String>> rs = DbBase.dbSelect(sql);
		if (0 == rs.size()) return null;

		return buildEventBean(rs.get(0));
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルのデータをBeanに格納する。
	 *
	 * @param hashMap 検索されたデータオブジェクト
	 * @return 値を格納したEventBeanオブジェクト
	 * @throws CompSysException フレームワーク共通例外
	 */
	private static EventBean buildEventBean(HashMap<String, String> hashMap)
	{
		EventBean bean = new EventBean();
		bean.setEventId(DbI.character(hashMap.get("event_id")));
		bean.setUserId(DbI.character(hashMap.get("user_id")));
		bean.setTitle(DbI.character(hashMap.get("title")));
		bean.setEventDate(DbI.ymd(hashMap.get("event_date")));
		bean.setPlace(DbI.character(hashMap.get("place")));
		bean.setStartTime(DbI.hm(hashMap.get("start_time")));
		bean.setEndTime(DbI.hm(hashMap.get("end_time")));
		bean.setGatherPlace(DbI.character(hashMap.get("gather_place")));
		bean.setGatherTime(DbI.hm(hashMap.get("gather_time")));
		bean.setAnswerTime(DbI.ymdhm(hashMap.get("answer_time")));
		bean.setOther(DbI.character(hashMap.get("other")));
		bean.setOutline(DbI.character(hashMap.get("outline")));
		bean.setForceClosed(DbI.character(hashMap.get("force_closed")));
		bean.setRegistTime(DbI.ymdhm(hashMap.get("regist_time")));
		bean.setUpdateTime(DbI.ymdhm(hashMap.get("update_time")));
		bean.setUpdateNum(DbI.num0(hashMap.get("update_flgs")));
		bean.setUserName(DbI.character(hashMap.get("name")));
		bean.setInvitationNum(DbI.num0(hashMap.get("invitationnum")));
		bean.setAttendanceCount(DbI.num0(hashMap.get("attendancecount")));
		bean.setAbsenceCount(DbI.num0(hashMap.get("absencecount")));
		bean.setReservationCount(DbI.num0(hashMap.get("reservationcount")));
		bean.setDeleteFlg(DbI.bool(hashMap.get("delete_flg")));
		bean.setBeforeSendDatetime(DbI.ymdhm(hashMap.get("before_send_datetime")));
		bean.setCapacity(DbI.num(hashMap.get("capacity")));
		bean.setSentFlg(DbI.bool(hashMap.get("sent_flg")));
		bean.setIsVotable(DbI.num0(hashMap.get("is_votable")));


		return bean;
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルにデータを挿入する。
	 *
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbInsert(EventBean bean) throws CompSysException
	{
		bean.setEventId(GetNumber.getNumber("tbl_event"));
		String sql = "insert into tbl_event ("
				+ " event_id"
				+ ",user_id"
				+ ",title"
				+ ",event_date"
				+ ",place"
				+ ",start_time"
				+ ",end_time"
				+ ",gather_place"
				+ ",gather_time"
				+ ",answer_time"
				+ ",other"
				+ ",outline"
				+ ",force_closed"
				+ ",regist_time"
				+ ",update_time"
				+ ",update_flgs"
				+ ",before_send_datetime"
				+ ",capacity"
				+ " ) values ( "
				+ DbO.character(bean.getEventId())
				+ "," + DbO.character(bean.getUserId())
				+ "," + DbO.character(bean.getTitle())
				+ "," + DbO.ymd(bean.getEventDate())
				+ "," + DbO.character(bean.getPlace())
				+ "," + DbO.hm(bean.getStartTime())
				+ "," + DbO.hm(bean.getEndTime())
				+ "," + DbO.character(bean.getGatherPlace())
				+ "," + DbO.hm(bean.getGatherTime())
				+ "," + DbO.ymdhm(bean.getAnswerTime())
				+ "," + DbO.character(bean.getOther())
				+ "," + DbO.character(bean.getOutline())
				+ "," + DbO.character(bean.getForceClosed()) // 田中編集箇所
				+ "," + "now()" // 登録日時
				+ "," + DbO.ymdhm(bean.getUpdateTime())
				+ "," + DbO.num0(bean.getUpdateNum()) // 田中編集箇所
				+ "," + DbO.ymdhm(bean.getBeforeSendDatetime())
				+ "," + DbO.num(bean.getCapacity())
				+ " )";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbInsert number or record exception.");
		return true;
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルのデータを更新する。
	 *
	 * @param pEventId イベントID
	 * @param bean EventBeanオブジェクト
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbUpdate(EventBean bean) throws CompSysException
	{
		String sql = "update tbl_event set "
				+ " event_id = " + DbO.character(bean.getEventId())
				+ "," + " user_id = " + DbO.character(bean.getUserId())
				+ "," + " title = " + DbO.character(bean.getTitle())
				+ "," + " event_date = " + DbO.ymd(bean.getEventDate())
				+ "," + " place = " + DbO.character(bean.getPlace())
				+ "," + " start_time = " + DbO.hm(bean.getStartTime())
				+ "," + " end_time = " + DbO.hm(bean.getEndTime())
				+ "," + " gather_place = " + DbO.character(bean.getGatherPlace())
				+ "," + " gather_time = " + DbO.hm(bean.getGatherTime())
				+ "," + " answer_time = " + DbO.ymdhm(bean.getAnswerTime())
				+ "," + " other = " + DbO.character(bean.getOther())
				+ "," + " outline = " + DbO.character(bean.getOutline())
				+ "," + " force_closed = " + DbO.character(bean.getForceClosed())
				+ "," + " regist_time = " + DbO.ymdhm(bean.getRegistTime())
				+ "," + " update_time = " + "now()" // 更新日時
				+ "," + " update_flgs = " + DbO.num0(bean.getUpdateNum())
				+ "," + " before_send_datetime = " + DbO.ymdhm(bean.getBeforeSendDatetime())
				+ "," + " capacity = " + DbO.num(bean.getCapacity())
				+ " where event_id = " + DbS.character(bean.getEventId())
				+ "";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbUpdate number or record exception.");
		return true;
	}

	/**
	 * tbl_event イベントの開催のデータを更新して復元する。
	 *
	 * @param pEventId イベントID
	 * @param bean EventBeanオブジェクト
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbUpdateForReborn(String eventId) throws CompSysException
	{
		String sql = "update tbl_event set "
				+ " delete_flg = false"
				+ " where event_id = " + DbS.character(eventId)
				+ "";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbUpdate number or record exception.");
		return true;
	}

	/**
	 * tbl_event イベントの開催のデータを更新して削除状態にする。
	 *
	 * @param pEventId イベントID
	 * @param bean EventBeanオブジェクト
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbUpdateForDelete(String eventId) throws CompSysException
	{
		String sql = "update tbl_event set "
				+ " delete_flg = true"
				+ " where event_id = " + DbS.character(eventId)
				+ "";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbUpdate number or record exception.");
		return true;
	}

	/**
	 * tbl_event イベントの開催のデータを更新して事前送信日時を削除する。
	 *
	 * @param pEventId イベントID
	 * @param bean EventBeanオブジェクト
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbUpdateDeleteBefor(String eventId) throws CompSysException
	{
		String sql = "update tbl_event set "
				+ " before_send_datetime=NULL"
				+ ",sent_flg=true"
				+ " where event_id = " + DbS.character(eventId)
				+ "";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbUpdate number or record exception.");
		return true;
	}

	public static boolean dbCount(String eventId) throws CompSysException
	{
		String sql = "update tbl_event set "
				+ "update_flgs = "
				+ "update_flgs + 1"
				+ " where event_id = " + DbS.character(eventId)
				+ "";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbUpdate number or record exception.");
		return true;
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルからデータを削除する。.
	 *
	 * @param pEventId イベントID
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbDelete(String pEventId) throws CompSysException
	{
		String sql = "delete from tbl_event "
				+ " where event_id = " + DbS.character(pEventId)
				+ "";
		int ret = DbBase.dbExec(sql);
		if (ret != 1) throw new CompSysException("dbDelete number or record exception.");
		return true;
	}

	/**
	 * テーブルからデータを削除する。.
	 *
	 * @param pUserId ユーザID
	 * @return true:成功 false:失敗
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static boolean dbDeleteByUserId(String pUserId) throws CompSysException
	{
		String sql = "";
		sql = "select event_id from tbl_event"
				+ " where user_id = " + DbS.character(pUserId);
		List<HashMap<String, String>> rs = DbBase.dbSelect(sql);
		if (rs.size() < 1) return true;
		for (int i = 0; i < rs.size(); i++)
		{
			EventMemberDao.dbDeleteForEventId(rs.get(i).get("event_id"));
			StatusDao.dbDeleteForEventId(rs.get(i).get("event_id"));
			CommentDao.dbDeleteForEventId(rs.get(i).get("event_id"));
		}
		sql = "delete from tbl_event "
				+ " where user_id = " + DbS.character(pUserId)
				+ "";
		DbBase.dbExec(sql);
		return true;
	}

	/**
	 * tbl_event 登録イベント一覧テーブルを検索し指定されたレコードのリストを返す。.
	 *
	 * @param pUserId ログインユーザーIDを検索条件にする
	 * @param pEventDate0 当月１日の日付を検索条件にする
	 * @param pEventDate1 翌月１日の日付を検索条件にする
	 * @return 取得したEventBeanの配列
	 * @throws CompSysException フレームワーク共通例外
	 */
	public static ArrayList<EventBean> dbSelectList(String pUserId, String pEventDate0, String pEventDate1) throws CompSysException
	{
		ArrayList<EventBean> array = new ArrayList<EventBean>(); // ArrayList型の配列EventBeanをインスタンス化してarrayに格納

		List<HashMap<String, String>> rs;
		String sql = "select "
				+ " tbl_event.event_id"
				+ ",tbl_event.user_id"
				+ ",tbl_event.title"
				+ ",tbl_event.event_date"
				+ ",tbl_event.start_time"
				+ ",tbl_event.update_flgs"
				+ ",tbl_event.sent_flg"
				+ " from tbl_event";
		sql += " WHERE tbl_event.user_id = " + DbS.character(pUserId); // ユーザIDを検索条件にする(ユーザが登録したイベントに限定する)
		sql += " AND tbl_event.event_date >= " + DbS.character(pEventDate0); // 開催日が表示月１日以降を検索条件にする
		sql += " AND tbl_event.event_date <= " + DbS.character(pEventDate1); // 開催日が表示月最終日以前を検索条件にする
		sql += " AND tbl_event.delete_flg = false ";
		sql += " order by tbl_event.start_time asc"; // イベント開始時間順にソート
		rs = DbBase.dbSelect(sql);
		int cnt = rs.size();
		if (cnt < 1) return array;
		for (int i = 0; i < cnt; i++)
		{
			array.add(buildEventBean(rs.get(i)));
		}
		return array;
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルを検索し指定されたレコードのリストを返す。.
	 *
	 * @param bean 検索条件をTblEventBeanのインスタンスに入れて渡す
	 * @param sortKey ソート順を配列で渡す　キー値は項目名　値はソート順 "ASC" "DESC"
	 * @param daoPageInfo 取得したいページの番やライン数を入れる。結果がここに帰ってくる
	 *        ライン数に-1を入れると全件取得になる
	 * @return 取得したTblEventDaoの配列
	 * @throws CompSysException フレームワーク共通例外
	 */
	static public ArrayList<EventBean> dbSelectList(EventBean bean, LinkedHashMap<String, String> sortKey, DaoPageInfo daoPageInfo) throws CompSysException
	{
		ArrayList<EventBean> array = new ArrayList<EventBean>();

		/* レコードの総件数を求める */
		String sql = "select count(*) as count"
				+ " from tbl_event "
				+ "right join tbl_user on tbl_event.user_id = tbl_user.user_id "
				+ dbWhere(bean);
		List<HashMap<String, String>> rs = DbBase.dbSelect(sql);
		if (0 == rs.size()) return array;
		HashMap<String, String> map = rs.get(0);
		int len = Integer.parseInt(map.get("count"));
		daoPageInfo.setRecordCount(len);
		if (len == 0) return array;
		if (-1 >= daoPageInfo.getLineCount()) daoPageInfo.setLineCount(len);
		daoPageInfo.setMaxPageNo((int) Math.ceil((double) len / (double) (daoPageInfo.getLineCount())));
		if (daoPageInfo.getPageNo() < 1) daoPageInfo.setPageNo(1);
		if (daoPageInfo.getPageNo() > daoPageInfo.getMaxPageNo()) daoPageInfo.setPageNo(daoPageInfo.getMaxPageNo());
		int start = (daoPageInfo.getPageNo() - 1) * daoPageInfo.getLineCount();
		sql = "select "
				+ " tbl_event.event_id"
				+ ",tbl_event.user_id"
				+ ",tbl_event.title"
				+ ",tbl_event.event_date"
				+ ",tbl_event.place"
				+ ",tbl_event.start_time"
				+ ",tbl_event.end_time"
				+ ",tbl_event.gather_place"
				+ ",tbl_event.gather_time"
				+ ",tbl_event.other"
				+ ",tbl_event.outline"
				+ ",tbl_event.force_closed"
				+ ",tbl_event.regist_time"
				+ ",tbl_event.update_time"
				+ ",tbl_event.answer_time"
				+ ",tbl_event.update_flgs"
				+ ",tbl_event.delete_flg"
				+ ",tbl_event.before_send_datetime"
				+ ",tbl_event.capacity"
				+ ",tbl_event.sent_flg"
				+ ",tbl_user.name"
				+ ",tbl_user.user_id"
				+ " from tbl_event "
				+ "left join tbl_user on tbl_event.user_id = tbl_user.user_id ";
		String where = dbWhere(bean);
		String order = dbOrder(sortKey);
		sql += where;
		sql += order;
		sql += " limit " + daoPageInfo.getLineCount() + " offset " + start + ";";
		rs = DbBase.dbSelect(sql);
		int cnt = rs.size();
		if (cnt < 1) return array;
		for (int i = 0; i < cnt; i++)
		{
			array.add(buildEventBean(rs.get(i)));
		}
		return array;
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルを検索し指定されたレコードのリストを返す。.
	 *
	 * @param bean 検索条件をTblEventBeanのインスタンスに入れて渡す
	 * @param sortKey ソート順を配列で渡す　キー値は項目名　値はソート順 "ASC" "DESC"
	 * @param daoPageInfo 取得したいページの番やライン数を入れる。結果がここに帰ってくる
	 *        ライン数に-1を入れると全件取得になる
	 * @return 取得したTblEventDaoの配列
	 * @throws CompSysException フレームワーク共通例外
	 */
	static public ArrayList<EventBean> dbSelectListForAdm(EventBean bean, LinkedHashMap<String, String> sortKey, DaoPageInfo daoPageInfo) throws CompSysException
	{
		ArrayList<EventBean> array = new ArrayList<EventBean>();

		/* レコードの総件数を求める */
		String sql = "select count(*) as count"
				+ " from tbl_event "
				+ "left join tbl_user on tbl_event.user_id = tbl_user.user_id "
				+ dbWhere(bean);
		List<HashMap<String, String>> rs = DbBase.dbSelect(sql);
		if (0 == rs.size()) return array;
		HashMap<String, String> map = rs.get(0);
		int len = Integer.parseInt(map.get("count"));
		daoPageInfo.setRecordCount(len);
		if (len == 0) return array;
		if (-1 >= daoPageInfo.getLineCount()) daoPageInfo.setLineCount(len);
		daoPageInfo.setMaxPageNo((int) Math.ceil((double) len / (double) (daoPageInfo.getLineCount())));
		if (daoPageInfo.getPageNo() < 1) daoPageInfo.setPageNo(1);
		if (daoPageInfo.getPageNo() > daoPageInfo.getMaxPageNo()) daoPageInfo.setPageNo(daoPageInfo.getMaxPageNo());
		int start = (daoPageInfo.getPageNo() - 1) * daoPageInfo.getLineCount();
		sql = "select "
				+ " tbl_event.event_id"
				+ ",tbl_event.user_id"
				+ ",tbl_event.title"
				+ ",tbl_event.event_date"
				+ ",tbl_event.place"
				+ ",tbl_event.start_time"
				+ ",tbl_event.delete_flg"
				+ ",tbl_event.sent_flg"
				+ ",tbl_user.name"
				+ ",COALESCE(cnttbl.cnt,0)"
				+ " as invitationnum"
				+ " from tbl_event "
				+ " left join tbl_user on tbl_event.user_id = tbl_user.user_id "
				+ " left join"
				+ "("
				+ " select count(tbl_event_member.event_id) as cnt,tbl_event.event_id from tbl_event"
				+ " left join tbl_event_member on tbl_event.event_id=tbl_event_member.event_id "
				+ " group by tbl_event.event_id"
				+ ")"
				+ "as cnttbl on cnttbl.event_id = tbl_event.event_id "
				+ "";
		sql += dbWhere(bean);
		sql += dbOrder(sortKey);
		sql += " limit " + daoPageInfo.getLineCount() + " offset " + start + ";";
		rs = DbBase.dbSelect(sql);
		int cnt = rs.size();
		if (cnt < 1) return array;
		for (int i = 0; i < cnt; i++)
		{
			array.add(buildEventBean(rs.get(i)));
		}
		return array;
	}

	static public ArrayList<EventBean> dbSelectListForSendMail() throws CompSysException
	{
		ArrayList<EventBean> array = new ArrayList<EventBean>();

		List<HashMap<String, String>> rs;
		String sql = "update tbl_event set "
				+ " before_send_datetime=NULL"
				+ " where event_id in"
				+ "(select event_id  "
				+ " from tbl_event "
				+ " where before_send_datetime <= " + DbS.ymdhm(new Date())
				+ " and tbl_event.delete_flg=true)"
				+ "";
		DbBase.dbExec(sql);
		sql = "select "
				+ " tbl_event.*"
				+ " from tbl_event "
				+ " where tbl_event.before_send_datetime <= " + DbS.ymdhm(new Date())
				+ " and tbl_event.delete_flg=false";
		rs = DbBase.dbSelect(sql);
		int cnt = rs.size();
		if (cnt < 1) return array;
		for (int i = 0; i < cnt; i++)
		{
			array.add(buildEventBean(rs.get(i)));
		}

		return array;
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルの検索条件を設定する。.
	 *
	 * @return String where句の文字列
	 * @throws CompSysException フレームワーク共通例外
	 */
	private static String dbWhere(EventBean bean) throws CompSysException
	{
		StringBuffer where = new StringBuffer(1024);
		if (bean.getEventId().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.event_id = " + DbS.character(bean.getEventId()));
		}
		if (bean.getUserId().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.user_id = " + DbS.character(bean.getUserId()));
		}
		if (bean.getUserName().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_user.name = " + DbS.character(bean.getUserName()));
		}

		if (bean.getTitle().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.title = " + DbS.character(bean.getTitle()));
		}

		if (bean.getEventDate().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.event_date =" + DbS.ymd(bean.getEventDate()));
		}

		if (bean.getPlace().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.place = " + DbS.character(bean.getPlace()));
		}

		if (bean.getStartTime().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.start_time =" + DbS.hm(bean.getStartTime()));
		}

		if (bean.getOutline().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.outline = " + DbS.character(bean.getOutline()));
		}

		if (bean.getForceClosed().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.force_closed =" + DbS.num(bean.getForceClosed()));
		}

		if (bean.getRegistTime().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.regist_time =" + DbS.ymdhm(bean.getRegistTime()));
		}

		if (bean.getUpdateTime().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.update_time =" + DbS.ymdhm(bean.getUpdateTime()));
		}

		if (bean.getUpdateNum().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.update_flgs =" + DbS.num(bean.getUpdateNum()));
		}
		if (bean.getDeleteFlg().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.delete_flg =" + bean.getDeleteFlg());
		}
		if (bean.getDateFrom().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.event_date >=" + DbS.ymd(bean.getDateFrom()));
		}
		if (bean.getDateTo().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("tbl_event.event_date <=" + DbS.ymd(bean.getDateTo()));
		}
		if (bean.getHolding().length() > 0)
		{
			Date now = new Date();
			SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
			String nowDate = date.format(now);
			where.append(where.length() > 0 ? " AND " : "");
			if (bean.getHolding().equals("Already"))
			{
				where.append("tbl_event.event_date <" + DbS.ymd(nowDate));
			}
			else if (bean.getHolding().equals("Ready"))
			{
				where.append("tbl_event.event_date >" + DbS.ymd(nowDate));
			}
			else if (bean.getHolding().equals("Now"))
			{
				where.append("tbl_event.event_date =" + DbS.ymd(nowDate));
			}
		}
		if (bean.getSearchKeyword().length() > 0)
		{
			where.append(where.length() > 0 ? " AND " : "");
			where.append("(");
			where.append(" tbl_user.name " + DbS.like(bean.getSearchKeyword()));
			where.append("or tbl_event.title " + DbS.like(bean.getSearchKeyword()));
			where.append("or tbl_event.place " + DbS.like(bean.getSearchKeyword()));
			where.append("or tbl_event.gather_place " + DbS.like(bean.getSearchKeyword()));
			where.append("or tbl_event.other " + DbS.like(bean.getSearchKeyword()));
			where.append("or tbl_event.outline " + DbS.like(bean.getSearchKeyword()));
			where.append(")");
		}
		if (where.length() > 0)
		{
			return " where " + where.toString();
		}
		return "";
	}

	/**
	 * tbl_event イベントの開催に関する各種値テーブルの並べ替え順序を設定する。.
	 *
	 * @param sortKey
	 * @return Stringソート句の文字列
	 */
	public static String dbOrder(LinkedHashMap<String, String> sortKey)
	{
		String str = "";
		if (sortKey == null) return "";
		Set<String> keySet = sortKey.keySet();
		for (Iterator<String> i = keySet.iterator(); i.hasNext();)
		{
			String key = i.next();
			if (null == fieldsArray.get(key)) continue;
			str += !"".equals(str) ? " , " : "";
			str += fieldsArray.get(key) + " " + sortKey.get(key);
		}
		str = "".equals(str) ? "" : (" order by " + str);
		return str;
	}
	
	public static String getVoteDeadlineByEventId(String eventId) throws CompSysException {
	    String sql = "SELECT vote_deadline FROM tbl_event WHERE event_id = " + DbS.character(eventId);
	    List<HashMap<String, String>> rs = DbBase.dbSelect(sql);
	    if (rs.size() == 0) return null;
	    return rs.get(0).get("vote_deadline");
	}
	
	public static void updateEventScheduleByMostVoted(String eventId) throws CompSysException {
	    // ① 最多得票候補を取得
	    HashMap<String, String> candidate = TblCandidateVotesDao.getMostVotedCandidateInfo(eventId);
	    if (candidate == null) return;

	    // ② イベントテーブルを更新
	    String sql = "UPDATE tbl_event SET " +
	                 "event_date = " + DbO.character(candidate.get("candidate_date")) + ", " +
	                 "start_time = " + DbO.character(candidate.get("start_time")) + ", " +
	                 "end_time = " + DbO.character(candidate.get("end_time")) + ", " +
	                 "gather_time = " + DbO.character(candidate.get("gather_time")) + " " +
	                 "WHERE event_id = " + DbS.character(eventId);

	    int result = DbBase.dbExec(sql);
	    if (result != 1) {
	        throw new CompSysException("投票結果によるイベント情報の更新に失敗しました。");
	    }
	}



}
	
