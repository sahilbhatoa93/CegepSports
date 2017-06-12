package methodCalls;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import utilities.DB;

/**
 * @author SAHIL BHATOA
 *
 * 
 */
public class Call {
	//private DB db = new DB("144.217.163.57", "sahil", "bhatoa");
	private DB db = new DB("localhost", "cegepSports", "sahil");
	public String validate(String username, String password) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs3 = db.fireQuery(connection,
					"select * from appuser full outer join student on appuser.email=student.email full outer join organiser on appuser.email=organiser.email where lower(appuser.email)"
							+ " =lower( '" + username + "') or lower(student.studentID) =lower( '" + username
							+ "') or lower(organiser.employeeID) =lower( '" + username + "')");
			rs3.last();
			if (rs3.getRow() == 1) {
				ResultSet rs = db.fireQuery(connection,
						"select * from appuser full outer join student on appuser.email=student.email full outer join organiser on"
								+ " appuser.email=organiser.email  where (lower(appuser.email) " + "=lower( '"
								+ username + "') or lower(student.studentID) =lower( '" + username + "') or "
								+ "lower(organiser.employeeID) =lower( '" + username + "')) and appuser.password='"
								+ password + "'");
				rs.last();
				if (rs.getRow() == 1) {
					rs.beforeFirst();
					while (rs.next()) {
						ResultSet rs2 = db.fireQuery(connection,
								"select * from TOURNAMENT inner join tournamentlocation on TOURNAMENT.id=tournamentlocation.TOURNAMENT_ID inner join location "
										+ "on tournamentlocation.location_id=location.id  where TOURNAMENT.START_DATE > (select sys_extract_utc(systimestamp) from dual)");
						JSONArray upComingEvents = new JSONArray();
						while (rs2.next()) {
							JSONObject event = new JSONObject();
							event.accumulate("Tournament ID", rs2.getString("id"));
							event.accumulate("Tournament Name", rs2.getString("name"));
							event.accumulate("Image", getBase64StringImage(rs2.getBlob("picture")));
							event.accumulate("Location", rs2.getString("address") + " " + rs2.getString("city") + " "
									+ rs2.getString("postalcode"));
							event.accumulate("StartDateTimestamp", db.getDateFromString(rs2.getString("start_date")));
							upComingEvents.add(event);
						}
						if (upComingEvents.size() != 0) {
							resultJSONObject.accumulate("Status", "OK");
							resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
							resultJSONObject.accumulate("Username",
									rs.getString("firstName") + " " + rs.getString("lastName"));
							resultJSONObject.accumulate("Email", rs.getString("email"));
							resultJSONObject.accumulate("ProfilePic", getBase64StringImage(rs.getBlob("displayImage")));
							if (rs.getString("employeeID") == null) {
								if (rs.getString("isPlayer").equals("0"))
									resultJSONObject.accumulate("ProfileType", "Student");
								else
									resultJSONObject.accumulate("ProfileType", "Player");
							} else
								resultJSONObject.accumulate("ProfileType", "Organiser");
							resultJSONObject.accumulate("UpComing Events", upComingEvents);
						} else {
							resultJSONObject.accumulate("Status", "WRONG");
							resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
							resultJSONObject.accumulate("Username",
									rs.getString("firstName") + " " + rs.getString("lastName"));
							resultJSONObject.accumulate("Email", rs.getString("email"));
							resultJSONObject.accumulate("ProfilePic", getBase64StringImage(rs.getBlob("displayImage")));
							resultJSONObject.accumulate("Message", "No UpComing Events Found");
						}

					}
				} else {
					resultJSONObject.accumulate("Status", "WRONG");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Password");
				}
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "Invalid UserName/User doesn't Exists");
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getBase64StringImage(Blob b) {
		String result = "";
		byte[] imgData = null;
		try {
			imgData = b.getBytes(1, (int) b.length());
			result = Base64.getEncoder().encodeToString(imgData);
		} catch (NullPointerException | SQLException e) {
			result = "No Image Found";
		}
		return result;
	}

	public String getTournamentDetails(String tournamentID) {
		JSONObject resultJSONObject = new JSONObject();

		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from tournament inner join organiser on organiser.email=tournament.ORGANISER_EMAIL "
							+ "inner join appuser on organiser.EMAIL=appuser.email  inner join tournamentlocation on TOURNAMENT.id=tournamentlocation.TOURNAMENT_ID"
							+ " inner join location on tournamentlocation.location_id=location.id where tournament.id='"
							+ tournamentID + "'");
			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				while (rs2.next()) {
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Tournament ID", rs2.getString("id"));
					resultJSONObject.accumulate("Tournament Name", rs2.getString("name"));
					resultJSONObject.accumulate("Description", rs2.getString("DESCRIPTION"));
					resultJSONObject.accumulate("Image", getBase64StringImage(rs2.getBlob("picture")));
					resultJSONObject.accumulate("Location",
							rs2.getString("address") + " " + rs2.getString("city") + " " + rs2.getString("postalcode"));
					resultJSONObject.accumulate("StartDateTimestamp",
							db.getDateFromString(rs2.getString("start_date")));
					resultJSONObject.accumulate("EndDateTimestamp", db.getDateFromString(rs2.getString("end_date")));
					JSONObject organiser = new JSONObject();
					organiser.accumulate("Name", rs2.getString("firstname") + " " + rs2.getString("lastname"));
					organiser.accumulate("Email", rs2.getString("email"));
					organiser.accumulate("Image", getBase64StringImage(rs2.getBlob("displayImage")));
					resultJSONObject.accumulate("Organiser", organiser);
				}
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Tournament Found");
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			e.printStackTrace();
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String rateTournament(String rating, String comment, String studentEmail, String tournamentID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"insert into rating values ('" + studentEmail + "','" + tournamentID + "'," + rating + ",'"
							+ comment + "',(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' +  numtodsinterval("
							+ db.getDateTimeStampInUTCFormat() + "/1000,'second')) AT time zone tz_offset('utc'),'1')",
					false, null, false);
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
			} else {

				if (operationResult.contains("unique constraint"))
					operationResult = db.fireInsertQuery(connection,
							"update rating set rating=" + rating + ",comments='" + comment
									+ "' , dateofcomment=(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' +  numtodsinterval("
									+ db.getDateTimeStampInUTCFormat()
									+ "/1000,'second')) AT time zone tz_offset('utc')" + " where studentEmail='"
									+ studentEmail + "' and tournamentID='" + tournamentID + "'",
							false, null, false);
				if (operationResult.equals("Success")) {
					resultJSONObject.accumulate("Status", "Ok");
					resultJSONObject.accumulate("Message", "Success");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("student Email", studentEmail);
				} else if (operationResult.contains("integrity constraint")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Information Provided");
					resultJSONObject.accumulate("student Email", studentEmail);
					resultJSONObject.accumulate("tournamentID", tournamentID);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("student Email", studentEmail);
				}

			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();
	}

	/*
	 * public String enrollAsPlayer(String studentID) { JSONObject
	 * resultJSONObject = new JSONObject(); try { Connection connection =
	 * db.createConnection(); String operationResult =
	 * db.fireInsertQuery(connection,
	 * "update student set isPlayer='1' where studentID='" + studentID + "'",
	 * false, null, false); if (operationResult.equals("Success")) {
	 * resultJSONObject.accumulate("Status", "OK");
	 * resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * operationResult); } else { resultJSONObject.accumulate("Status",
	 * "Wrong"); resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * db.formatErrorCode(operationResult));
	 * resultJSONObject.accumulate("studentID", studentID); }
	 * db.closeConnection(connection); } catch (Exception e) {
	 * e.printStackTrace(); resultJSONObject.accumulate("Status", "ERROR");
	 * resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * db.formatErrorCode(e.getMessage()));
	 * 
	 * } return resultJSONObject.toString(); }
	 */

	public String getTournamentsAndRating(String tournamentID) {

		JSONObject resultJSONObject = new JSONObject();

		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from TOURNAMENT full outer join rating on TOURNAMENT.id=rating.TOURNAMENTID where tournament.id='"
							+ tournamentID + "' ");

			rs2.last();
			if (rs2.getRow() >= 1) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				rs2.beforeFirst();
				ArrayList<String> tournamentList = new ArrayList<>();
				while (rs2.next()) {

					if (tournamentList.contains(rs2.getString("id"))) {
						JSONArray ratingAndComments = resultJSONObject.getJSONArray("RatingAndComments");

						if (!(rs2.getString("rating") == null && rs2.getString("rating") == null)) {
							JSONObject ratingAndComment = new JSONObject();
							ratingAndComment.accumulate("Rating", rs2.getString("rating"));
							ratingAndComment.accumulate("Comment", rs2.getString("comments"));
							ratingAndComment.accumulate("Date Of Comment",
									db.getDateFromString(rs2.getString("dateofcomment")));
							ratingAndComment.accumulate("Student Email", rs2.getString("studentemail"));
							ratingAndComments.add(ratingAndComment);
						}

					} else {
						resultJSONObject.accumulate("Tournament ID", rs2.getString("id"));
						resultJSONObject.accumulate("Tournament Name", rs2.getString("name"));
						resultJSONObject.accumulate("Image", getBase64StringImage(rs2.getBlob("picture")));
						JSONArray ratingAndComments = new JSONArray();

						if (!(rs2.getString("rating") == null && rs2.getString("rating") == null)) {
							JSONObject ratingAndComment = new JSONObject();
							ratingAndComment.accumulate("Rating", rs2.getString("rating"));
							ratingAndComment.accumulate("Comment", rs2.getString("comments"));
							ratingAndComment.accumulate("Date Of Comment",
									db.getDateFromString(rs2.getString("dateofcomment")));
							ratingAndComment.accumulate("Student Email", rs2.getString("studentemail"));
							ratingAndComments.add(ratingAndComment);
						}

						resultJSONObject.accumulate("RatingAndComments", ratingAndComments);
						tournamentList.add(rs2.getString("id"));
					}
				}
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Tournaments Added");
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String searchTournaments(String gameType) {
		JSONObject resultJSONObject = new JSONObject();

		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from TOURNAMENT inner  join sports on TOURNAMENT.SPORTS_ID=sports.id inner join tournamentlocation on TOURNAMENT.id=tournamentlocation.TOURNAMENT_ID "
							+ "INNER JOIN LOCATION on tournamentlocation.location_id=location.id where LOWER(sports.name)=LOWER('"
							+ gameType + "')");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray tournaments = new JSONArray();
				while (rs2.next()) {
					JSONObject tournament = new JSONObject();
					tournament.accumulate("Tournament ID", rs2.getString("id"));
					tournament.accumulate("Tournament Name", rs2.getString("name"));
					tournament.accumulate("Image", getBase64StringImage(rs2.getBlob("picture")));
					tournament.accumulate("Location",
							rs2.getString("address") + " " + rs2.getString("city") + " " + rs2.getString("postalcode"));
					tournament.accumulate("StartDateTimestamp", db.getDateFromString(rs2.getString("start_date")));
					tournaments.add(tournament);
				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Tournaments", tournaments);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Tournaments Found");
				resultJSONObject.accumulate("Searched Game", gameType);
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getTeamStats(String teamID) {
		JSONObject resultJSONObject = new JSONObject();

		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select tournament.id as tournamentID,tournament.name as tournamentName,tournament.picture as tournamentPicture,matches.id as matchesID,matches.MATCH_DATE as matchesmatchDate,matchesResult.IS_WIN "
							+ "as matchResult,statistics.STUDENT_EMAIL as studentEmail,STATCATEGORY.name as statisticsCategory,statistics.totalpoints as statisticsPoints,(select name from matchesResult inner join teams "
							+ "on matchesResult.team_id=teams.id where team_id != '" + teamID
							+ "' and matchesResult.MATCH_ID=matches.id) as opponentTeam, (select score from matchesresult where match_id=statistics.MATCH_ID and team_id=(select id from matchesResult "
							+ "inner join teams on matchesResult.team_id=teams.id where team_id != '" + teamID
							+ "'  and matchesResult.MATCH_ID=matches.id)) as opponentTeamScore from tournament inner join teamtournament on tournament.id=teamtournament.tOURNAMENT_ID "
							+ "inner join teams on teams.id=teamtournament.team_id left outer join matchesResult on teams.id=matchesResult.team_id left outer join  matches on matches.id=matchesResult.match_id inner join statistics "
							+ "on matches.id=statistics.MATCH_ID   inner join STATCATEGORY on STATCATEGORY.id=statistics.stats_id   where teamtournament.team_id = '"
							+ teamID + "'  and "
							+ "statistics.STUDENT_EMAIL in (select student_email from member where team_id='" + teamID
							+ "' and REQUESTSTATUS='Accepted') and teamtournament.REQUESTSTATUS='Accepted'");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray tournaments = new JSONArray();
				ArrayList<String> tournamentList = new ArrayList<>();
				ArrayList<String> matchIDList = new ArrayList<>();
				ArrayList<String> studentIDList = new ArrayList<>();
				while (rs2.next()) {
					if (tournamentList.contains(rs2.getString("tournamentID"))) {
						JSONArray matches = tournaments
								.getJSONObject(tournamentList.indexOf(rs2.getString("tournamentID")))
								.getJSONArray("Matches");
						if (matchIDList.contains(rs2.getString("matchesID"))) {
							JSONArray playerStatistics = matches
									.getJSONObject(matchIDList.indexOf(rs2.getString("matchesID")))
									.getJSONArray("Player Statistics");
							if (studentIDList.contains(rs2.getString("studentEmail"))) {
								JSONObject playerStatistic = playerStatistics
										.getJSONObject(studentIDList.indexOf(rs2.getString("studentEmail")));
								playerStatistic.accumulate(rs2.getString("statisticsCategory"),
										rs2.getString("statisticsPoints"));
								// playerStatistics.add(playerStatistic);

							} else {
								JSONObject playerStatistic = new JSONObject();
								playerStatistic.accumulate("Student Email", rs2.getString("studentEmail"));
								playerStatistic.accumulate(rs2.getString("statisticsCategory"),
										rs2.getString("statisticsPoints"));
								playerStatistics.add(playerStatistic);
							}
						} else {
							JSONObject match = new JSONObject();
							match.accumulate("Match ID", rs2.getString("matchesID"));
							match.accumulate("Match Date", db.getDateFromString(rs2.getString("matchesmatchDate")));
							match.accumulate("Opponent Team", rs2.getString("opponentTeam"));
							match.accumulate("Opponent Team Score", rs2.getString("opponentTeamScore"));
							match.accumulate("Result", rs2.getString("matchResult"));
							JSONArray playerStatistics = new JSONArray();
							JSONObject playerStatistic = new JSONObject();
							playerStatistic.accumulate("Student Email", rs2.getString("studentEmail"));
							playerStatistic.accumulate(rs2.getString("statisticsCategory"),
									rs2.getString("statisticsPoints"));
							playerStatistics.add(playerStatistic);
							match.accumulate("Player Statistics", playerStatistics);
							matches.add(match);
						}
					} else {
						JSONObject tournament = new JSONObject();
						tournament.accumulate("Tournament ID", rs2.getString("tournamentID"));
						tournament.accumulate("Tournament Name", rs2.getString("tournamentName"));
						tournament.accumulate("Image", getBase64StringImage(rs2.getBlob("tournamentPicture")));
						JSONArray matches = new JSONArray();
						JSONObject match = new JSONObject();
						match.accumulate("Match ID", rs2.getString("matchesID"));
						match.accumulate("Match Date", db.getDateFromString(rs2.getString("matchesmatchDate")));
						match.accumulate("Opponent Team", rs2.getString("opponentTeam"));
						match.accumulate("Opponent Team Score", rs2.getString("opponentTeamScore"));
						if (rs2.getString("matchResult").equals("1"))
							match.accumulate("Result", "Won");
						else if (rs2.getString("matchResult").equals("0"))
							match.accumulate("Result", "Lost");
						else
							match.accumulate("Result", "Draw/Tie");
						JSONArray playerStatistics = new JSONArray();
						JSONObject playerStatistic = new JSONObject();
						playerStatistic.accumulate("Student Email", rs2.getString("studentEmail"));
						playerStatistic.accumulate(rs2.getString("statisticsCategory"),
								rs2.getString("statisticsPoints"));
						playerStatistics.add(playerStatistic);
						match.accumulate("Player Statistics", playerStatistics);
						matches.add(match);
						tournament.accumulate("Matches", matches);
						tournaments.add(tournament);
						tournamentList.add(rs2.getString("tournamentID"));
						matchIDList.add(rs2.getString("matchesID"));
						studentIDList.add(rs2.getString("studentEmail"));
					}

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Tournaments", tournaments);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Teams Found");
				resultJSONObject.accumulate("Searched Team ID", teamID);
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
			resultJSONObject.accumulate("Searched Team ID", teamID);
		}
		return resultJSONObject.toString();
	}

	public String getTeamDetails(String teamID) {
		JSONObject resultJSONObject = new JSONObject();
		String teamName = "";
		long registerDate = 0L;
		String base64encodedImageString = "";

		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from teams left outer join member on teams.id=member.TEAM_ID left outer join student on student.email=member.STUDENT_Email left outer "
							+ " join appuser on student.EMAIL=appuser.EMAIL where teams.id='" + teamID
							+ "' and member.STATUS=1 and member.requeststatus='Accepted'");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray players = new JSONArray();
				while (rs2.next()) {
					JSONObject player = new JSONObject();
					player.accumulate("Name", rs2.getString("firstName") + " " + rs2.getString("lastName"));
					player.accumulate("Display Image", getBase64StringImage(rs2.getBlob("displayimage")));
					player.accumulate("Student Email", rs2.getString("email"));
					players.add(player);
					teamName = rs2.getString("name");
					registerDate = db.getDateFromString(rs2.getString("register_date"));
					base64encodedImageString = getBase64StringImage(rs2.getBlob("team_picture"));
				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Team Name", teamName);
				resultJSONObject.accumulate("Team Register Date", registerDate);
				resultJSONObject.accumulate("Team Image", base64encodedImageString);
				resultJSONObject.accumulate("Players", players);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Team Found");
				resultJSONObject.accumulate("Searched Team ID", teamID);
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getAllPlayers(String studentID) {

		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = null;
			if (studentID.equals("null"))
				rs2 = db.fireQuery(connection,
						"select * from student  inner   join appuser on student.EMAIL=appuser.EMAIL where student.ISPLAYER=1 ");
			else
				rs2 = db.fireQuery(connection,
						"select * from student  inner   join appuser on student.EMAIL=appuser.EMAIL where student.ISPLAYER=1 and student.studentID='"
								+ studentID + "'");
			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray players = new JSONArray();
				while (rs2.next()) {
					JSONObject player = new JSONObject();
					player.accumulate("Name", rs2.getString("firstName") + " " + rs2.getString("lastName"));
					player.accumulate("Display Image", getBase64StringImage(rs2.getBlob("displayimage")));
					player.accumulate("Student ID", rs2.getString("studentid"));
					players.add(player);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Players", players);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Players Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getPlayerStats(String studentID) {

		JSONObject resultJSONObject = new JSONObject();

		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select  tournament.id as tournamentID,tournament.name as tournamentName,tournament.picture as tournamentPicture,matches.id as matchesID,matches.MATCH_DATE as "
							+ "matchesmatchDate,matchesResult.IS_WIN as matchResult,student.STUDENTID as studentID,statcategory.name as statisticsCategory,statistics.totalpoints as"
							+ " statisticsPoints from tournament inner join teamtournament on tournament.id=teamtournament.tOURNAMENT_ID inner join teams on teams.id=teamtournament.team_id inner join "
							+ "matchesResult on teams.id=matchesResult.team_id inner join  matches on matches.id=matchesResult.match_id inner join statistics on matches.id=statistics.MATCH_ID "
							+ " inner join student on student.email=statistics.student_email inner join statcategory on statcategory.id=statistics.stats_id where  student.STUDENTID='"
							+ studentID + "'");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray tournaments = new JSONArray();
				ArrayList<String> tournamentList = new ArrayList<>();
				ArrayList<String> matchIDList = new ArrayList<>();
				ArrayList<String> studentIDList = new ArrayList<>();
				while (rs2.next()) {
					if (tournamentList.contains(rs2.getString("tournamentID"))) {
						JSONArray matches = tournaments
								.getJSONObject(tournamentList.indexOf(rs2.getString("tournamentID")))
								.getJSONArray("Matches");
						if (matchIDList.contains(rs2.getString("matchesID"))) {
							JSONArray playerStatistics = matches
									.getJSONObject(matchIDList.indexOf(rs2.getString("matchesID")))
									.getJSONArray("Player Statistics");
							if (studentIDList.contains(rs2.getString("studentID"))) {
								JSONObject playerStatistic = playerStatistics
										.getJSONObject(studentIDList.indexOf(rs2.getString("studentID")));
								playerStatistic.accumulate(rs2.getString("statisticsCategory"),
										rs2.getString("statisticsPoints"));
							} else {
								JSONObject playerStatistic = new JSONObject();
								playerStatistic.accumulate("Student ID", rs2.getString("studentID"));
								playerStatistic.accumulate(rs2.getString("statisticsCategory"),
										rs2.getString("statisticsPoints"));
								playerStatistics.add(playerStatistic);
							}
						} else {
							JSONObject match = new JSONObject();
							match.accumulate("Match ID", rs2.getString("matchesID"));
							match.accumulate("Match Date", db.getDateFromString(rs2.getString("matchesmatchDate")));
							// match.accumulate("Opponent Team",
							// rs2.getString("opponentTeam"));
							match.accumulate("Result", rs2.getString("matchResult"));
							JSONArray playerStatistics = new JSONArray();
							JSONObject playerStatistic = new JSONObject();
							playerStatistic.accumulate("Student ID", rs2.getString("studentID"));
							playerStatistic.accumulate(rs2.getString("statisticsCategory"),
									rs2.getString("statisticsPoints"));
							playerStatistics.add(playerStatistic);
							match.accumulate("Player Statistics", playerStatistics);
							matches.add(match);
						}
					} else {
						JSONObject tournament = new JSONObject();
						tournament.accumulate("Tournament ID", rs2.getString("tournamentID"));
						tournament.accumulate("Tournament Name", rs2.getString("tournamentName"));
						tournament.accumulate("Image", getBase64StringImage(rs2.getBlob("tournamentPicture")));
						JSONArray matches = new JSONArray();
						JSONObject match = new JSONObject();
						match.accumulate("Match ID", rs2.getString("matchesID"));
						match.accumulate("Match Date", db.getDateFromString(rs2.getString("matchesmatchDate")));
						// match.accumulate("Opponent Team",
						// rs2.getString("opponentTeam"));
						if (rs2.getString("matchResult").equals("1"))
							match.accumulate("Result", "Won");
						else if (rs2.getString("matchResult").equals("0"))
							match.accumulate("Result", "Lost");
						else
							match.accumulate("Result", "Draw/Tie");
						JSONArray playerStatistics = new JSONArray();
						JSONObject playerStatistic = new JSONObject();
						// playerStatistic.accumulate("Student ID",
						// rs2.getString("studentID"));
						playerStatistic.accumulate(rs2.getString("statisticsCategory"),
								rs2.getString("statisticsPoints"));
						playerStatistics.add(playerStatistic);
						match.accumulate("Player Statistics", playerStatistics);
						matches.add(match);
						tournament.accumulate("Matches", matches);
						tournaments.add(tournament);
						tournamentList.add(rs2.getString("tournamentID"));
						matchIDList.add(rs2.getString("matchesID"));
						studentIDList.add(rs2.getString("studentID"));
					}

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Tournaments", tournaments);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Activity Found");
				resultJSONObject.accumulate("Searched ID", studentID);
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String denrollAsPlayer(String studentID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"update student set isPlayer='0' where studentID='" + studentID + "'", false, null, false);
			if (operationResult.equals("Success")) {
				operationResult = db.fireInsertQuery(connection,
						"update member set status='0' , IS_CAPTAIN='0' where STUDENT_email= (select email from student where studentid='"
								+ studentID + "')",
						false, null, false);

				if (operationResult.equals("Success")) {
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
									+ "',concat((select email from student where studentid='" + studentID + "'),"
									+ "'has denrolled from the team'),'0',(select email from student where studentid='"
									+ studentID + "'),'Information')",
							false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select student_email from member where team_id=(select team_id from member where student_email=(select email from student where studentid='"
									+ studentID + "')"
									+ " and status='0' and is_captain='0' and requeststatus='Accepted') and is_captain='1'),(select email from student where studentid='"
									+ studentID + "')" + ",'0')",
							false, null, false);
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", db.formatErrorCode(operationResult));
					resultJSONObject.accumulate("studentID", studentID);
				}
			} else {
				resultJSONObject.accumulate("Status", "Wrong");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", db.formatErrorCode(operationResult));
				resultJSONObject.accumulate("studentID", studentID);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();
	}

	public String getChatHistory(String ID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from appuser inner join student on appuser.EMAIL=student.EMAIL where appuser.email in (select MESSAGE_RECEIVER.RECEIVEREMAIL from MESSAGE_RECEIVER inner join appuser on appuser.email=MESSAGE_RECEIVER.SENDEREMAIL "
							+ "inner join student on student.EMAIL=appuser.EMAIL where student.STUDENTID='" + ID
							+ "')");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray chats = new JSONArray();
				while (rs2.next()) {
					JSONObject chat = new JSONObject();
					chat.accumulate("Name", rs2.getString("firstName") + " " + rs2.getString("lastName"));
					chat.accumulate("Display Image", getBase64StringImage(rs2.getBlob("displayimage")));
					chat.accumulate("Student ID", rs2.getString("studentid"));
					chats.add(chat);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Sender Student ID", ID);
				resultJSONObject.accumulate("Chats", chats);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Sender Student ID", ID);
				resultJSONObject.accumulate("Message", "No Chats Found");
			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getChatContacts(String ID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs = db.fireQuery(connection, "select * from organiser where employeeid='" + ID + "'");
			rs.last();
			ResultSet rs2 = null;
			if (rs.getRow() >= 1)
				rs2 = db.fireQuery(connection,
						"(select appuser.firstname,appuser.lastname,appuser.displayImage,member.student_email as student_email from tournament inner join TEAMTOURNAMENT on TEAMTOURNAMENT.TOURNAMENT_ID=tournament.ID inner join teams on teams.id=TEAMTOURNAMENT.TEAM_ID inner join member on teams.ID=member.team_id "
								+ "inner join appuser on appuser.EMAIL=member.STUDENT_EMAIL where tournament.ORGANISER_EMAIL =(select email from organiser where employeeid='"+ID+"')) "
								+ "union  all "
								+ "(select appuser.firstname,appuser.lastname,appuser.displayImage,organiser.EMAIL from organiser inner join appuser on "
								+ "appuser.EMAIL=organiser.EMAIL where employeeid !='"+ID+"' )");

			else
				rs2 = db.fireQuery(connection,
						"select * from member inner join teams on teams.id=member.team_id inner join student on "
								+ "member.student_email=student.email inner join appuser on appuser.EMAIL=student.EMAIL where student.STUDENTID !='"
								+ ID + "' and "
								+ "member.TEAM_ID in (select team_id from member where student_email=(select email from student where studentid='"
								+ ID + "'))");
			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray chats = new JSONArray();
				while (rs2.next()) {
					JSONObject chat = new JSONObject();
					chat.accumulate("Name", rs2.getString("firstName") + " " + rs2.getString("lastName"));
					chat.accumulate("Display Image", getBase64StringImage(rs2.getBlob("displayimage")));
					chat.accumulate("Student Email", rs2.getString("student_email"));
					chats.add(chat);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Chat Contacts", chats);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Contacts Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getChat(String senderID, String receiverID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from MESSAGE_RECEIVER inner join appuser on appuser.email=MESSAGE_RECEIVER.SENDEREMAIL inner join student on "
							+ "student.EMAIL=appuser.EMAIL inner join message_history on message_history.senderemail=MESSAGE_RECEIVER.senderemail  where (student.STUDENTID='"
							+ senderID + "' or student.STUDENTID='" + receiverID + "' ) and "
							+ "(MESSAGE_RECEIVER.RECEIVEREMAIL in (select email from student where studentid='"
							+ senderID + "') or "
							+ "MESSAGE_RECEIVER.RECEIVEREMAIL in (select email from student where studentid='"
							+ receiverID + "')) order by MESSAGE_RECEIVER.TIME_STAMP");
			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray chats = new JSONArray();
				while (rs2.next()) {
					JSONObject chat = new JSONObject();
					chat.accumulate("Name", rs2.getString("firstName") + " " + rs2.getString("lastName"));
					chat.accumulate("Message TimeStamp", db.getDateFromString(rs2.getString("time_stamp")));
					chat.accumulate("Message Content", rs2.getString("messagecontent"));
					chats.add(chat);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Chat Contacts", chats);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Chats Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String createTeam(String studentID, String teamName, String gameType, String teamImage) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"INSERT INTO teams VALUES (NULL, '" + teamName
							+ "', (TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' +  numtodsinterval("
							+ db.getDateTimeStampInUTCFormat() + "/1000,'second')) AT time zone tz_offset('utc')"
							+ ",?, '1', (select id from sports where lower(name)=lower('" + gameType + "')),'Pending')",
					true, getInputStreamFromBase64String(teamImage), false);
			if (operationResult.equals("Success")) {
				operationResult = db.fireInsertQuery(connection,
						"INSERT INTO member VALUES(null,(select email from student where studentid='" + studentID
								+ "'),(select id from teams where name='" + teamName
								+ "' and requeststatus='Pending'), (TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' +  numtodsinterval("
								+ db.getDateTimeStampInUTCFormat() + "/1000,'second'))"
								+ " AT time zone tz_offset('utc'), '1', '1','Accepted')",
						false, null, false);
				if (operationResult.equals("Success")) {
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					operationResult = db.fireInsertQuery(connection, "insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
							+ "',concat((select email from student where studentid='" + studentID + "'),"
							+ "'has requested to create team '" + teamName
							+ "'),'0',(select email from student where studentid='" + studentID + "'),'Request')",
							false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select email from organiser where email = ( select max(email) from organiser )),"
									+ "(select email from student where studentid='" + studentID + "')" + ",'0')",
							false, null, false);
				} else if (operationResult.contains("integrity constraint")) {
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Student ID");
					operationResult = db.fireInsertQuery(connection,
							"delete from teams where id=(select id from teams where name='" + teamName
									+ "' and requeststatus='Pending')",
							false, null, false);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("studentID", studentID);
				}
			} else {
				resultJSONObject.accumulate("Status", "Wrong");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				resultJSONObject.accumulate("studentID", studentID);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String getMyTeams(String studentID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select teams.name as teamName,teams.id as teamID,sports.name as gameType,teams.TEAM_PICTURE as teamImage from  member inner join teams on member.TEAM_ID=teams.ID inner join"
							+ " sports on sports.id=teams.SPORTS_ID where member.STUDENT_email=(select email from student where studentid='"
							+ studentID + "') and member.status=1 and member.requestStatus='Accepted'");
			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray teams = new JSONArray();
				while (rs2.next()) {
					JSONObject team = new JSONObject();
					team.accumulate("Team Name", rs2.getString("teamName"));
					team.accumulate("Team ID", rs2.getString("teamID"));
					team.accumulate("Game Type", rs2.getString("gameType"));
					team.accumulate("Team Picture", getBase64StringImage(rs2.getBlob("teamImage")));
					teams.add(team);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Teams", teams);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Teams Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();

	}

	public String removePlayerFromTeam(String studentID, String teamID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"update member set status='0' where STUDENT_email=(select email from student where studentid='"
							+ studentID + "') and team_id='" + teamID + "'",
					false, null, false);
			if (operationResult.equals("Success")) {
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
								+ "',concat(concat((select email from student where studentid='" + studentID + "'),"
								+ "'has removed you from team),(select name from teams where id='" + teamID
								+ "' and requeststatus='Accepted')),'0',(select student_email from member where team_id='"
								+ teamID + "' and is_captain='1'" + " and requeststatus='Accepted'),'Information')",
						false, null, false);
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
								+ "',(select email from student where studentid='" + studentID + "'),"
								+ "(select student_email from member where team_id='" + teamID + "' and is_captain='1'"
								+ " and requeststatus='Accepted')" + ",'0')",
						false, null, false);
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
			} else {
				resultJSONObject.accumulate("Status", "Wrong");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				if (operationResult.equals("Failed"))
					resultJSONObject.accumulate("Message", "Invalid Student/Team ID");
				else
					resultJSONObject.accumulate("Message", operationResult);
				resultJSONObject.accumulate("studentID", studentID);
				resultJSONObject.accumulate("teamID", teamID);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String joinTeam(String studentID, String teamID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"begin INSERT  INTO member VALUES((select email from student where studentid='" + studentID + "'),'"
							+ teamID + "', (TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' + " + " numtodsinterval("
							+ db.getDateTimeStampInUTCFormat()
							+ "/1000,'second')) AT time zone tz_offset('utc'), '1', '0','Pending'); exception when dup_val_on_index then null ;end;",
					false, null, false);
			if (operationResult.equals("Success")) {
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
								+ "',concat(concat((select email from student where studentid='" + studentID + "'),"
								+ "'has requested to join team),(select name from teams where id='" + teamID
								+ "' and requeststatus='Accepted')),'0',(select email from student where studentid='"
								+ studentID + "')",
						false, null, false);
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
								+ "',(select student_email from member where team_id='" + teamID
								+ "' and is_captain='1'" + " and requeststatus='Accepted'),"
								+ "(select email from student where studentid='" + studentID + "')" + ",'0')",
						false, null, false);
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
			} else {
				if (operationResult.contains("integrity constraint")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Team ID");
					resultJSONObject.accumulate("studentID", studentID);
					resultJSONObject.accumulate("teamID", teamID);
				} else if (operationResult.contains("cannot insert NULL")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Student ID");
					resultJSONObject.accumulate("studentID", studentID);
					resultJSONObject.accumulate("teamID", teamID);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("studentID", studentID);
				}

			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String reactToJoinTeamRequest(String ID, String reactBoolean) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = null;
			if (reactBoolean.equalsIgnoreCase("true")) {
				operationResult = db.fireInsertQuery(connection,
						"update member set requeststatus='Accepted' where id='" + ID + "'",
						false, null, false);
			} else {
				operationResult = db.fireInsertQuery(connection,
						"update member set requeststatus='Declined' where id='" + ID + "'",
						false, null, false);
			}
			if (operationResult.equals("Success")) {
				String receiverEmail="";
				String teamID="";
				ResultSet rs2 = db.fireQuery(connection,
						"select student_email,team_id from member where id='" + ID + "'");
					while (rs2.next()) {
						receiverEmail=rs2.getString("student_email");
						teamID=rs2.getString("team_id");
					}
					System.out.println("insert into MESSAGE_HISTORY values ('" + db.getTimeStamp()
							+ "',concat( 'Request Accepted to Join the team',(select name from teams where id='" + teamID
							+ "' and requeststatus='Accepted')),'0',(select student_email from member where team_id='"
							+ teamID + "' and is_captain='1'" + " and requeststatus='Accepted'),'Information')");
				if (reactBoolean.equalsIgnoreCase("true"))
					operationResult = db.fireInsertQuery(connection, "insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
							+ "',concat( 'Request Accepted to Join the team',(select name from teams where id='" + teamID
							+ "' and requeststatus='Accepted')),'0',(select student_email from member where team_id='"
							+ teamID + "' and is_captain='1'" + " and requeststatus='Accepted'),'Information')", false,
							null, false);
				else
					operationResult = db.fireInsertQuery(connection, "insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
							+ "',concat( 'Request Denied to Join the team',(select name from teams where id='" + teamID
							+ "' and requeststatus='Accepted')),'0',(select student_email from member where team_id='"
							+ teamID + "' and is_captain='1'" + " and requeststatus='Accepted'),'Information')", false,
							null, false);
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
								+ "','" + receiverEmail + "',"
								+ "(select student_email from member where team_id='" + teamID + "' and is_captain='1'"
								+ " and requeststatus='Accepted'),'0')",
						false, null, false);
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
			} else {
				
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String createTeamByOrganiser(String captainID, String teamName, String gameType, String teamImage,
			String organiserID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"INSERT INTO teams VALUES (NULL, '" + teamName
							+ "',  (TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' + " + " numtodsinterval("
							+ db.getDateTimeStampInUTCFormat()
							+ "/1000,'second')) AT time zone tz_offset('utc'),?, '1', (select id from sports where name='"
							+ gameType + "'),'Accepted')",
					true, getInputStreamFromBase64String(teamImage), false);
			if (operationResult.equals("Success")) {
				operationResult = db.fireInsertQuery(connection,
						"INSERT INTO member VALUES(null,(select email from student where studentid='" + captainID
								+ "'),(select id from teams where name='" + teamName
								+ "' and requeststatus='Accepted'),(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' + "
								+ " numtodsinterval(" + db.getDateTimeStampInUTCFormat()
								+ "/1000,'second')) AT time zone tz_offset('utc'), '1', '1','Accepted')",
						false, null, false);
				if (operationResult.equals("Success")) {
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
									+ "',concat((select email from organiser where employeeid='" + organiserID + "'),"
									+ "'has created team '" + teamName
									+ "' with you as Captain),'0',(select email from organiser where employeeid='"
									+ organiserID + "','Information')",
							false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select email from student where studentid='" + captainID + "'),"
									+ "(select email from organiser where employeeid='" + organiserID + "')" + ",'0')",
							false, null, false);
				} else if (operationResult.contains("integrity constraint")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Sports Type");
					resultJSONObject.accumulate("studentID", captainID);
				} else if (operationResult.contains("cannot insert NULL")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Student ID/Sports Type");
					resultJSONObject.accumulate("studentID", captainID);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("studentID", captainID);
				}
				operationResult = db.fireInsertQuery(connection,
						"delete from teams where id=(select id from teams where name='" + teamName
								+ "' and requeststatus='Accepted')",
						false, null, false);
			} else {
				if (operationResult.contains("integrity constraint")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Student ID");
					resultJSONObject.accumulate("studentID", captainID);
				} else if (operationResult.contains("cannot insert NULL")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Sports Type");
					resultJSONObject.accumulate("studentID", captainID);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("studentID", captainID);
				}
				operationResult = db.fireInsertQuery(connection,
						"delete from teams where id=(select id from teams where name='" + teamName
								+ "' and requeststatus='Accepted')",
						false, null, false);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String reactToTeamCreateRequest(String teamID, String reactBoolean, String organiserID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = null;
			if (reactBoolean.equalsIgnoreCase("true")) {
				operationResult = db.fireInsertQuery(connection,
						"update teams set requeststatus='Accepted' where  id='" + teamID + "'", false, null, false);
			} else {
				operationResult = db.fireInsertQuery(connection,
						"delete from member  where team_id='" + teamID + "' and requeststatus='Pending'", false, null,
						false);
				operationResult = db.fireInsertQuery(connection,
						"update teams set requeststatus='Declined' where  id='" + teamID + "'", false, null, false);
			}
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				if (reactBoolean.equalsIgnoreCase("true")) {
					operationResult = db.fireInsertQuery(connection, "insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
							+ "',concat( Request Accepted to create team,(select name from teams where id='" + teamID
							+ "' and requeststatus='Accepted')),'0',(select email from organiser where employeeid='"
							+ organiserID + "'),'Information')", false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select student_email from member where team_id='" + teamID
									+ "' and is_captain='1'" + " and requeststatus='Accepted'),"
									+ "(select email from organiser where employeeid='" + organiserID + "')" + ",'0')",
							false, null, false);

				} else {
					operationResult = db.fireInsertQuery(connection, "insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
							+ "',concat( Request Denied create the team,(select name from teams where id='" + teamID
							+ "' and requeststatus='Accepted')),'0',(select email from organiser where employeeid='"
							+ organiserID + "'),'Information')", false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select student_email from member where team_id='" + teamID
									+ "' and is_captain='1'" + " and requeststatus='Declined'),"
									+ "(select email from organiser where employeeid='" + organiserID + "')" + ",'0')",
							false, null, false);
				}

			} else {
				resultJSONObject.accumulate("Status", "Wrong");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				resultJSONObject.accumulate("teamID", teamID);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String createTournament(String tournamentName, String tournamentDescription, String gameType,
			String tournmantImage, String startDate, String endDate, String minPlayer, String maxPlayer,
			String organiserID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"INSERT INTO tournament  VALUES (NULL,'" + tournamentName + "','" + tournamentDescription
							+ "',(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' +  numtodsinterval(" + startDate
							+ "/1000,'second')) AT time zone tz_offset('utc'),"
							+ "(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' +  numtodsinterval(" + endDate
							+ "/1000,'second')) AT time zone tz_offset('utc'), ?,'" + maxPlayer + "','" + minPlayer
							+ "', '1'," + " (select id from sports where lower(name)=lower('" + gameType
							+ "')),(select email from organiser where employeeid= '" + organiserID + "'))",
					true, getInputStreamFromBase64String(tournmantImage), false);
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				String dataTime = db.getTimeStamp();
				operationResult = db.fireInsertQuery(connection, "insert into MESSAGE_HISTORY values('" + dataTime
						+ "',concat((select email from organiser where employeeid='" + organiserID + "'),"
						+ "'has created New Tournament '" + tournamentName
						+ "'),'0',(select email from organiser where employeeid='" + organiserID + "','Information')",
						false, null, false);
				ResultSet rs2 = db.fireQuery(connection,
						"select student_email from member inner join teams on teams.ID=member.team_id inner join "
								+ "sports on teams.SPORTS_ID=sports.ID where member.IS_CAPTAIN='1' and teams.REQUESTSTATUS = 'Accepted' and sports.id=(select id from sports where lower(name)=lower('"
								+ gameType + "'))");
				while (rs2.next()) {
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + dataTime + "','" + rs2.getString("student_email") + "'),"
									+ "(select email from organiser where employeeid='" + organiserID + "')" + ",'0')",
							false, null, false);
				}
			} else {
				if (operationResult.contains("cannot insert NULL")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Organiser ID/Sports Type");
					resultJSONObject.accumulate("organiserID", organiserID);
				} else if (operationResult.contains("column not allowed here")) {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Date Entered");
					resultJSONObject.accumulate("startDate", startDate);
					resultJSONObject.accumulate("endDate", endDate);
				}

				else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("tournamentName", tournamentName);
				}

			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();
	}

	public String createSportsName(String sportName) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"INSERT INTO sports  VALUES (NULL,lower('" + sportName + "'),'1')", false, null, false);
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);

			} else {
				if (operationResult.contains("unique constraint")) {
					{
						resultJSONObject.accumulate("Status", "OK");
						resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
						resultJSONObject.accumulate("Message", "Sports Already Exists");
					}
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
				}
				resultJSONObject.accumulate("sportName", sportName);
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();
	}

	public String signUp(String firstname, String lastname, String email, String iD, String password, String profilePic,
			String profileType) {
		JSONObject resultJSONObject = new JSONObject();

		try {
			Connection connection = db.createConnection();
			String operationResult = db
					.fireInsertQuery(
							connection, "insert into appuser values ('" + email + "','" + password + "','" + firstname
									+ "','" + lastname + "',?,'1')",
							true, getInputStreamFromBase64String(profilePic), false);
			if (operationResult.equals("Success")) {
				if (profileType.equalsIgnoreCase("student"))
					operationResult = db.fireInsertQuery(connection,
							"insert into student values ('" + iD + "','0','" + email + "','1')", false, null, false);
				else
					operationResult = db.fireInsertQuery(connection,
							"insert into organiser values ('" + iD + "','" + email + "','1')", false, null, false);
				if (operationResult.equals("Success")) {
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					if (operationResult.contains("unique constraint"))
						resultJSONObject.accumulate("Message", "Username Already Exists");
					else
						resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("ID", iD);
				}
			}
		} catch (Exception e) {
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String modifyAccount(String firstname, String lastname, String email, String password, String profilePic,
			String studentID) {
		JSONObject resultJSONObject = new JSONObject();
		String userOldEmail = "";
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection, "select email from student where studentID='" + studentID + "' ");
			while (rs2.next()) {
				userOldEmail = rs2.getString("email");
			}
			String operationResult = db.fireInsertQuery(connection,
					"update appuser set firstname='" + firstname + "',lastname='" + lastname + "',password='" + password
							+ "',displayImage=? where email='" + userOldEmail + "'",
					true, getInputStreamFromBase64String(profilePic), true);
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
			} else {
				resultJSONObject.accumulate("Status", "Wrong");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				if (operationResult.contains("unique constraint"))
					resultJSONObject.accumulate("Message", "Email Already Exists");
				else
					resultJSONObject.accumulate("Message", operationResult);
				resultJSONObject.accumulate("Username", firstname + " " + lastname);
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public InputStream getInputStreamFromBase64String(String base64ImageString) {
		InputStream imageInputStream = null;
		byte[] imageBytearray = Base64.getDecoder().decode(base64ImageString);
		imageInputStream = new ByteArrayInputStream(imageBytearray);
		return imageInputStream;
	}

	/*
	 * public String updateUser(String name, String email, String username,
	 * String password, String profilePic, String dateOfBirth) { JSONObject
	 * resultJSONObject = new JSONObject();
	 * 
	 * try { Connection connection = db.createConnection(); String
	 * operationResult = db.fireInsertQuery(connection,
	 * "update appuser set user_id='" + username + "',name='" + name +
	 * "',email='" + email + "',password='" + password + "',dateofbirth'" +
	 * dateOfBirth + "'" + ",?", true,
	 * getInputStreamFromBase64String(profilePic), true);
	 * 
	 * if (operationResult.equals("Success")) {
	 * resultJSONObject.accumulate("Status", "OK");
	 * resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * operationResult); } else { resultJSONObject.accumulate("Status",
	 * "Wrong"); resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * operationResult); resultJSONObject.accumulate("Username", username); } }
	 * catch (Exception e) { resultJSONObject.accumulate("Status", "ERROR");
	 * resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * db.formatErrorCode(e.getMessage())); } return
	 * resultJSONObject.toString(); }
	 * 
	 * public String resetUserAccount(String username) { JSONObject
	 * resultJSONObject = new JSONObject(); try { Connection connection =
	 * db.createConnection(); String operationResult =
	 * db.fireInsertQuery(connection,
	 * "update appuser set userpssword='12345' where user_id='" + username +
	 * "'", false, null, false);
	 * 
	 * if (operationResult.equals("Success")) {
	 * resultJSONObject.accumulate("Status", "OK");
	 * resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * operationResult); } else { resultJSONObject.accumulate("Status",
	 * "Wrong"); resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * operationResult); resultJSONObject.accumulate("Username", username); } }
	 * catch (Exception e) { resultJSONObject.accumulate("Status", "ERROR");
	 * resultJSONObject.accumulate("Timestamp",
	 * db.getDateTimeStampInUTCFormat()); resultJSONObject.accumulate("Message",
	 * db.formatErrorCode(e.getMessage()));
	 * 
	 * } return resultJSONObject.toString(); }
	 */

	public String getTeamsAndSportsWithSearch(String searchField) {

		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 =null;
			if (searchField.equals("null"))
			 rs2 = db.fireQuery(connection,
					"select sports.name as sportName,teams.NAME as teamName,teams.ID as teamID from sports left outer join teams on teams.SPORTS_ID=sports.ID");
			else
				 rs2 = db.fireQuery(connection,
						"select sports.name as sportName,teams.NAME as teamName,teams.ID as teamID from sports left outer join teams on teams.SPORTS_ID=sports.ID where teams.name='"+searchField+"'");
					
			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONObject teamsAndGames = new JSONObject();
				while (rs2.next()) {
					JSONObject game = new JSONObject();
					JSONArray teams = new JSONArray();
					JSONObject team = new JSONObject();
					if (!(rs2.getString("teamID") == null || rs2.getString("teamName") == null)) {
						team.accumulate("Team ID", rs2.getString("teamID"));
						team.accumulate("Team Name", rs2.getString("teamName"));
						teams.add(team);
					}
					game.accumulate("Teams", teams);
					teamsAndGames.accumulate(rs2.getString("sportName"), game);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("TeamsAndGames", teamsAndGames);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Sports Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getTeamsAndSports(String studentID) {

		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select sports.name as sportName,teams.NAME as teamName,teams.ID as teamID from sports left outer join teams on "
							+ "teams.SPORTS_ID=sports.ID inner join member on member.TEAM_ID=teams.ID where member.STUDENT_email=(select email from student where studentid='"
							+ studentID + "') and member.status='1' and member.requeststatus='Accepted'");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONObject teamsAndGames = new JSONObject();
				while (rs2.next()) {
					JSONObject game = new JSONObject();
					JSONArray teams = new JSONArray();
					JSONObject team = new JSONObject();
					if (!(rs2.getString("teamID") == null || rs2.getString("teamName") == null)) {
						team.accumulate("Team ID", rs2.getString("teamID"));
						team.accumulate("Team Name", rs2.getString("teamName"));
						teams.add(team);
					}
					game.accumulate("Teams", teams);
					teamsAndGames.accumulate(rs2.getString("sportName"), game);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("TeamsAndGames", teamsAndGames);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Sports Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String joinTournament(String teamID, String tournamentID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = db.fireInsertQuery(connection,
					"begin INSERT INTO teamTournament VALUES(null,'" + teamID + "','" + tournamentID
							+ "','1',(TIMESTAMP '1970-01-01 00:00:00' AT TIME ZONE 'UTC' + " + " numtodsinterval("
							+ db.getDateTimeStampInUTCFormat()
							+ "/1000,'second')) AT time zone tz_offset('utc'),'Pending'); exception when dup_val_on_index then null ;end;",
					false, null, false);
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
								+ "',concat(concat((select student_email from member where team_id='" + teamID
								+ "' and is_captain='1'" + " and requeststatus='Accepted'),"
								+ "'has requested to join Tournament),(select name from tournament where id='"
								+ tournamentID + "')),'0',(select student_email from member where team_id='" + teamID
								+ "' and is_captain='1'" + " and requeststatus='Accepted'),'Request')",
						false, null, false);
				operationResult = db.fireInsertQuery(connection,
						"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
								+ "',(select organiser_email from tournament where id='" + tournamentID + "'),"
								+ "(select student_email from member where team_id='" + teamID + "' and is_captain='1'"
								+ " and requeststatus='Accepted')" + ",'0')",
						false, null, false);
			} else {
				if (operationResult.contains("integrity constraint")) {
					resultJSONObject.accumulate("Status", "OK");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", "Invalid Team/Tournament ID");
					resultJSONObject.accumulate("team ID", teamID);
					resultJSONObject.accumulate("tournament ID", tournamentID);

				} else {
					resultJSONObject.accumulate("Status", "Wrong");
					resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
					resultJSONObject.accumulate("Message", operationResult);
					resultJSONObject.accumulate("team ID", teamID);
				}

			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String reactToJoinTournamentRequest(String ID, String reactBoolean,
			String organiserID) {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			String operationResult = null;
			if (reactBoolean.equalsIgnoreCase("true")) {
				operationResult = db.fireInsertQuery(connection,
						"update teamtournament set requeststatus='Accepted' where id='" + ID + "'",
						false, null, false);
			} else {
				operationResult = db.fireInsertQuery(connection,
						"update teamtournament set requeststatus='Declined' where id='" + ID + "'",
						false, null, false);
			}
			if (operationResult.equals("Success")) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				if (reactBoolean.equalsIgnoreCase("true")) {
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
									+ "',concat( Request Accepted to Join TOurnament,(select name from tournament where id=(SELECT TOURNAMENT_ID FROM TEAM_TOURNAMENT WHERE ID='"
									+ ID + "'))),'0'," + "(select email from organiser where employeeid='"
									+ organiserID + "'),'Information')",
							false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select student_email from member where team_id=(SELECT TEAM_ID FROM TEAM_TOURNAMENT WHERE ID='"
									+ ID + "') and is_captain='1'" + " and requeststatus='Accepted'),"
									+ "(select email from organiser where employeeid='" + organiserID + "')" + ",'0')",
							false, null, false);

				} else {
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_HISTORY values('" + db.getTimeStamp()
									+ "',concat( Request Denied to Join TOurnament,(select name from tournament where id=(SELECT TOURNAMENT_ID FROM TEAM_TOURNAMENT WHERE ID='"
									+ ID + "'))),'0'," + "(select email from organiser where employeeid='"
									+ organiserID + "'),'Information')",
							false, null, false);
					operationResult = db.fireInsertQuery(connection,
							"insert into MESSAGE_RECEIVER values('" + db.getTimeStamp()
									+ "',(select student_email from member where team_id=(SELECT TEAM_ID FROM TEAM_TOURNAMENT WHERE ID='"
									+ ID + "') and is_captain='1'" + " and requeststatus='Declined'),"
									+ "(select email from organiser where employeeid='" + organiserID + "')" + ",'0')",
							false, null, false);
				}
			} else {
				resultJSONObject.accumulate("Status", "Wrong");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", operationResult);
				resultJSONObject.accumulate("teamtournamentID", ID);
				
			}
			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));

		}
		return resultJSONObject.toString();

	}

	public String getSportsAndTournaments(String studentID) {

		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select sports.name as sportName,TOURNAMENT.NAME as tournamentName,TOURNAMENT.ID as tournamentID from sports left outer join "
							+ "TOURNAMENT on TOURNAMENT.SPORTS_ID=sports.ID inner join teams on teams.SPORTS_ID=sports.ID "
							+ "inner join member on member.TEAM_ID=teams.ID where member.STUDENT_email=(select email from student where studentid='"
							+ studentID
							+ "') and member.status='1' and member.requeststatus='Accepted' and member.IS_CAPTAIN='1' and TOURNAMENT.START_DATE>current_date");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONObject sportsAndTournaments = new JSONObject();
				while (rs2.next()) {
					JSONObject game = new JSONObject();
					JSONArray tournaments = new JSONArray();
					JSONObject tournament = new JSONObject();
					if (!(rs2.getString("tournamentID") == null || rs2.getString("tournamentName") == null)) {
						tournament.accumulate("Tournament ID", rs2.getString("tournamentID"));
						tournament.accumulate("Tournament Name", rs2.getString("tournamentName"));
						tournaments.add(tournament);
					}
					game.accumulate("Tournaments", tournaments);
					sportsAndTournaments.accumulate(rs2.getString("sportName"), game);

				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("SportsAndTournaments", sportsAndTournaments);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Sports Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getChatAndNotifications(String studentID) {

		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from MESSAGE_HISTORY inner join MESSAGE_RECEIVER on MESSAGE_HISTORY.SENDEREMAIL=MESSAGE_RECEIVER.SENDEREMAIL where"
							+ " MESSAGE_RECEIVER.RECEIVEREMAIL=(select email from student where studentid='" + studentID
							+ "')  order by MESSAGE_HISTORY.TIME_STAMP desc");

			rs2.last();
			if (rs2.getRow() >= 1) {
				rs2.beforeFirst();
				JSONArray notificatons = new JSONArray();
				while (rs2.next()) {
					JSONObject notificaton = new JSONObject();
					notificaton.accumulate("Content", rs2.getString("messagecontent"));
					notificaton.accumulate("Action TimeStamp", rs2.getString("time_stamp"));
					notificaton.accumulate("Notification Type", rs2.getString("notificationtype"));
					notificaton.accumulate("Sender  Email ID", rs2.getString("senderemail"));
					notificatons.add(notificaton);
				}
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Notifications", notificatons);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Notifications Found");

			}

			db.closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

	public String getAllTournaments() {
		JSONObject resultJSONObject = new JSONObject();
		try {
			Connection connection = db.createConnection();
			ResultSet rs2 = db.fireQuery(connection,
					"select * from TOURNAMENT inner join tournamentlocation on TOURNAMENT.id=tournamentlocation.TOURNAMENT_ID inner join location "
							+ "on tournamentlocation.location_id=location.id  where TOURNAMENT.start_date < (select sys_extract_utc(systimestamp) from dual)");
			JSONArray upComingEvents = new JSONArray();
			while (rs2.next()) {
				JSONObject event = new JSONObject();
				event.accumulate("Tournament ID", rs2.getString("id"));
				event.accumulate("Tournament Name", rs2.getString("name"));
				event.accumulate("Image", getBase64StringImage(rs2.getBlob("picture")));
				event.accumulate("Location",
						rs2.getString("address") + " " + rs2.getString("city") + " " + rs2.getString("postalcode"));
				event.accumulate("StartDateTimestamp", db.getDateFromString(rs2.getString("start_date")));
				upComingEvents.add(event);
			}
			if (upComingEvents.size() != 0) {
				resultJSONObject.accumulate("Status", "OK");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Tournaments", upComingEvents);
			} else {
				resultJSONObject.accumulate("Status", "WRONG");
				resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
				resultJSONObject.accumulate("Message", "No Tournaments Found");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultJSONObject.accumulate("Status", "ERROR");
			resultJSONObject.accumulate("Timestamp", db.getDateTimeStampInUTCFormat());
			resultJSONObject.accumulate("Message", db.formatErrorCode(e.getMessage()));
		}
		return resultJSONObject.toString();
	}

}
