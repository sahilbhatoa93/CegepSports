package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import methodCalls.Call;

/**
 * @author SAHIL BHATOA
 *
 * 
 */
@Path("/RestService")
public class UserService {
	
	
	

	@GET
	@Path("/validateCrendentials&{username}&{password}")
	@Produces(MediaType.APPLICATION_JSON)
	public String validateCrendentials(@PathParam("username") String username, @PathParam("password") String password)
			throws FileNotFoundException, SQLException, IOException {
	
		Call call = new Call();
		return call.validate(username, password);
	}
	
	
	@GET
	@Path("/getTournamentDetails&{tournamentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTournamentDetails(@PathParam("tournamentID") String tournamentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getTournamentDetails(tournamentID);
	}

	
	@GET
	@Path("/rateTournament&{rating}&{comment}&{studentEmail}&{tournamentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String rateTournament(@PathParam("rating") String rating,
			@PathParam("comment") String comment,@PathParam("studentEmail") String studentEmail,@PathParam("tournamentID") String tournamentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.rateTournament(rating, comment,studentEmail,tournamentID);
	}
	
	/*@GET
	@Path("/enrollAsPlayer&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String enrollAsPlayer(@PathParam("studentID") String studentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.enrollAsPlayer(studentID);
	}*/
	
	@GET
	@Path("/getTournamentsAndRating&{tournamentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTournamentsAndRating(@PathParam("tournamentID") String tournamentID) throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getTournamentsAndRating(tournamentID);
	}
	

	
	@GET
	@Path("/searchTournaments&{gameType}")
	@Produces(MediaType.APPLICATION_JSON)
	public String searchTournaments(@PathParam("gameType") String gameType)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.searchTournaments(gameType);
	}
	
	
	@GET
	@Path("/getTeamStats&{teamID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTeamStats(@PathParam("teamID") String teamID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getTeamStats(teamID);
	}
	
	@GET
	@Path("/getTeamDetails&{teamID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTeamDetails(@PathParam("teamID") String teamID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getTeamDetails(teamID);
	}
	
	@GET
	@Path("/getAllPlayers&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllPlayers(@PathParam("studentID") String studentID) throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getAllPlayers(studentID);
	}
	
	@GET
	@Path("/getPlayerStats&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPlayerStats(@PathParam("studentID") String studentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getPlayerStats(studentID);
	}
	
	@GET
	@Path("/denrollAsPlayer&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String denrollAsPlayer(@PathParam("studentID") String studentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.denrollAsPlayer(studentID);
	}
	
	@GET
	@Path("/getChatHistory&{ID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChatHistory(@PathParam("ID") String ID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getChatHistory(ID);
	}
	
	@GET
	@Path("/getChatContacts&{ID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChatContacts(@PathParam("ID") String ID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getChatContacts(ID);
	}
	
	@GET
	@Path("/getChat&{senderID}&{receiverID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChat(@PathParam("senderID") String studentID,@PathParam("receiverID") String receiverID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getChat(studentID,receiverID);
	}

	
	@GET
	@Path("/createTeam&{studentID}&{teamName}&{gameType}&{teamImage}")
	@Produces(MediaType.APPLICATION_JSON)
	public String createTeam(@PathParam("studentID") String studentID, @PathParam("teamName") String teamName,
			 @PathParam("gameType") String gameType,
			@PathParam("teamImage") String teamImage) throws FileNotFoundException, SQLException, IOException {
		
		Call call = new Call();
		return call.createTeam(studentID, teamName, gameType, teamImage);
	}
	
	
	@GET
	@Path("/getMyTeams&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getMyTeams(@PathParam("studentID") String studentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getMyTeams(studentID);
	}
	
	@GET
	@Path("/removePlayerFromTeam&{studentID}&{teamID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String removePlayerFromTeam(@PathParam("studentID") String studentID,@PathParam("teamID") String teamID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.removePlayerFromTeam(studentID,teamID);
	}
	
	
	@GET
	@Path("/joinTeam&{studentID}&{teamID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String joinTeam(@PathParam("studentID") String studentID,@PathParam("teamID") String teamID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.joinTeam(studentID,teamID);
	}
	
	
	@GET
	@Path("/reactToJoinTeamRequest&{ID}&{reactBoolean}")
	@Produces(MediaType.APPLICATION_JSON)
	public String reactToJoinTeamRequest(@PathParam("ID") String ID,@PathParam("reactBoolean") String reactBoolean )
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.reactToJoinTeamRequest(ID,reactBoolean);
	}
	
	@GET
	@Path("/createTeamByOrganiser&{captainID}&{teamName}&{gameType}&{teamImage}&{organiserID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String createTeamByOrganiser(@PathParam("captainID") String captainID, @PathParam("teamName") String teamName,
			 @PathParam("gameType") String gameType,
			@PathParam("teamImage") String teamImage,@PathParam("organiserID") String organiserID) throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.createTeamByOrganiser(captainID, teamName, gameType, teamImage,organiserID);
	}
	
	
	@GET
	@Path("/reactToTeamCreateRequest&{teamID}&{reactBoolean}&{organiserID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String reactToTeamCreateRequest(@PathParam("teamID") String teamID,@PathParam("reactBoolean") String reactBoolean,@PathParam("organiserID") String organiserID )
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.reactToTeamCreateRequest(teamID,reactBoolean,organiserID);
	}
	
	
	@GET
	@Path("/createTournament&{tournamentName}&{tournamentDescription}&{gameType}&{tournmantImage}&{startDate}&{endDate}&{minPlayer}&{maxPlayer}&{organiserID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String createTournament(@PathParam("tournamentName") String tournamentName,@PathParam("tournamentDescription") String tournamentDescription,@PathParam("gameType") String gameType,
			@PathParam("tournmantImage") String tournmantImage,@PathParam("startDate") String startDate,@PathParam("endDate") String endDate,
			@PathParam("minPlayer") String minPlayer,@PathParam("maxPlayer") String maxPlayer,@PathParam("organiserID") String organiserID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.createTournament(tournamentName,tournamentDescription,gameType,tournmantImage,startDate,endDate,minPlayer,maxPlayer,organiserID);
	}
	
	@GET
	@Path("/modifyAccount&{firstname}&{lastname}&{email}&{ID}&{password}&{profilePic}")
	@Produces(MediaType.APPLICATION_JSON)
	public String modifyAccount(@PathParam("firstname") String firstname, @PathParam("lastname") String lastname,
			@PathParam("email") String email, @PathParam("ID") String ID,
			@PathParam("password") String password, @PathParam("profilePic") String profilePic)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.modifyAccount( firstname, lastname,  email,  password,  profilePic,  ID);
	}

	

	@GET
	@Path("/createSportsName&{sportName}")
	@Produces(MediaType.APPLICATION_JSON)
	public String createSportsName(@PathParam("sportName") String sportName)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.createSportsName(sportName);
	}
	
	
	@GET
	@Path("/getChatAndNotifications&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getChatAndNotifications(@PathParam("studentID") String studentID)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getChatAndNotifications(studentID);
	}
	
	
	
	@GET
	@Path("/getTeamsAndSportsWithSearch&{searchField}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTeamsAndSportsWithSearch(@PathParam("searchField") String searchField) throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getTeamsAndSportsWithSearch(searchField);
	}
	
	@GET
	@Path("/getTeamsAndSports&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTeamsAndSports(@PathParam("studentID") String studentID) throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getTeamsAndSports(studentID);
	}
	
	@GET
	@Path("/joinTournament&{teamID}&{tournamentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String joinTournament(@PathParam("teamID") String teamID,@PathParam("tournamentID") String tournamentID )
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.joinTournament(teamID,tournamentID);
	}
	
	@GET
	@Path("/reactToJoinTournamentRequest&{teamID}&{tournamentID}&{reactBoolean}&{organiserID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String reactToJoinTournamentRequest(@PathParam("ID") String ID,@PathParam("reactBoolean") String reactBoolean,@PathParam("organiserID") String organiserID )
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.reactToJoinTournamentRequest(ID ,reactBoolean,organiserID);
	}
	
	@GET
	@Path("/getSportsAndTournaments&{studentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getSportsAndTournaments(@PathParam("studentID") String studentID) throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getSportsAndTournaments(studentID);
	}
	
	
	
	
	@GET
	@Path("/signUp&{firstname}&{lastname}&{email}&{ID}&{password}&{profilePic}&{profileType}")
	@Produces(MediaType.APPLICATION_JSON)
	public String signUp(@PathParam("firstname") String firstname, @PathParam("lastname") String lastname,
			@PathParam("email") String email, @PathParam("ID") String ID,
			@PathParam("password") String password, @PathParam("profilePic") String profilePic, @PathParam("profileType") String profileType)
			throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.signUp(firstname, lastname, email, ID, password, profilePic,profileType);
	}
	
	
	@GET
	@Path("/getAllTournaments")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTournaments() throws FileNotFoundException, SQLException, IOException {
		Call call = new Call();
		return call.getAllTournaments();
	}
	
	
	
	

}