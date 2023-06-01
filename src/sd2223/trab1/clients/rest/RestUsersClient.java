package sd2223.trab1.clients.rest;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.UsersService;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;


public class RestUsersClient extends RestClient implements Users {

	final WebTarget target;
	
	public RestUsersClient( URI serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( UsersService.PATH );
	}
	
	private Result<String> clt_createUser( User user) {
		
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		return super.toJavaResult(r, String.class);
	}
	
	private Result<User> clt_getUser(String name, String pwd) {

		Response r = target.path( name )
				.queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		return super.toJavaResult(r, User.class);
	}
	
	private Result<User> clt_updateUser(String userId, String password, User user) {
		Response r = target.path( userId ).path(UsersService.PWD)
				.queryParam(UsersService.PWD, password).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));

		return super.toJavaResult(r, User.class);
	}

	private Result<User> clt_deleteUser(String userId, String pwd) {
		Response r = target.path( userId ).path(UsersService.PWD)
				.queryParam(UsersService.PWD, pwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();

		return super.toJavaResult(r, User.class);
	}

	@SuppressWarnings("unchecked")
	private Result<List<User>> clt_searchUsers(String pattern) {
		Response r = target.queryParam(UsersService.QUERY, pattern).request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		Class<List<User>> entityClass = (Class<List<User>>) (Object) List.class;
		return super.toJavaResult(r, entityClass);
	}
	
	@Override
	public Result<String> createUser(User user) {
		return super.reTry(() -> clt_createUser(user));
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		return super.reTry(() -> clt_getUser(name, pwd));
	}
	
	@Override
	public Result<Void> verifyPassword(String name, String pwd) {
		throw new RuntimeException("Not Implemented...");
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		return super.reTry(() -> clt_updateUser(userId, password, user));
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry(() -> clt_deleteUser(userId, password));
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry(() -> clt_searchUsers(pattern));
	}	
}
