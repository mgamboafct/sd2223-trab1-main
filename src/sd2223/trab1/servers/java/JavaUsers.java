package sd2223.trab1.servers.java;

import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Result.ErrorCode;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.FeedsClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JavaUsers implements Users {
	private final ConcurrentHashMap<String,User> users = new ConcurrentHashMap<>();

	private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

	private final String domain;

	public JavaUsers(String domain) {
		this.domain = domain;
	}

	@Override
	public Result<String> createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user data is valid
		if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
			Log.info("User object invalid.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		synchronized (users) {
			// Insert user, checking if name already exists
			if (users.putIfAbsent(user.getName(), user) != null) {
				Log.info("User already exists.");
				return Result.error(ErrorCode.CONFLICT);
			}

			// Create users feed
			Feeds feedsService = FeedsClientFactory.get(domain);
			feedsService.createFeed(user.getName() + "@" + domain);

			return Result.ok(user.getName() + "@" + user.getDomain());
		}
	}

	@Override
	public Result<User> getUser(String name, String pwd) {
		Log.info("getUser : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if(name == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		User user = users.get(name);
		// Check if user exists
		if( user == null ) {
			Log.info("User does not exist.");
			return Result.error( ErrorCode.NOT_FOUND);
		}

		//Check if the password is correct
		if( !user.getPwd().equals( pwd)) {
			Log.info("Password is incorrect.");
			return Result.error( ErrorCode.FORBIDDEN);
		}

		return Result.ok(user);
	}

	@Override
	public Result<User> updateUser(String name, String pwd, User user) {
		Log.info("updateUser : " + user);

		// Check if user is valid
		if(name == null || pwd == null || !user.getName().equals(name)) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		synchronized (users) {
			User stored = users.get(name);
			// Check if user exists
			if (stored == null) {
				Log.info("User does not exist.");
				return Result.error(ErrorCode.NOT_FOUND);
			}

			//Check if the password is correct
			if (!stored.getPwd().equals(pwd)) {
				Log.info("Password is incorrect.");
				return Result.error(ErrorCode.FORBIDDEN);
			}

			String newDisplayName = user.getDisplayName();
			String newPwd = user.getPwd();

			// Update users display name
			if (newDisplayName != null) {
				stored.setDisplayName(newDisplayName);
			}

			// Update users password
			if (newPwd != null) {
				stored.setPwd(newPwd);
			}

			// Save updated user
			users.put(name, stored);


			return Result.ok(stored);
		}

	}

	@Override
	public Result<User> deleteUser(String name, String pwd) {
		Log.info("deleteUser : user = " + name + "; pwd = " + pwd);

		// Check if user is valid
		if(name == null || pwd == null) {
			Log.info("Name or Password null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		synchronized (users) {
			User user = users.get(name);
			// Check if user exists
			if (user == null) {
				Log.info("User does not exist.");
				return Result.error(ErrorCode.NOT_FOUND);
			}

			//Check if the password is correct
			if (!user.getPwd().equals(pwd)) {
				Log.info("Password is incorrect.");
				return Result.error(ErrorCode.FORBIDDEN);
			}

			// Delete user
			users.remove(name);

			// Delete users feed
			Feeds feedsService = FeedsClientFactory.get(domain);
			feedsService.deleteFeed(name + "@" + domain);

			return Result.ok(user);
		}
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Log.info("searchUsers with pattern : " + pattern);

		// Check if user is valid
		if(pattern == null) {
			Log.info("Pattern is null.");
			return Result.error( ErrorCode.BAD_REQUEST);
		}

		List<User> matches = new ArrayList<>();
		// Find all users with names that contain the pattern
		for(User user : users.values()){
			String name = user.getName();
			if(name.contains(pattern)) {
				matches.add(user);
			}
		}

		return Result.ok(matches);
	}

	@Override
	public Result<Void> verifyPassword(String name, String pwd) {
		var res = getUser(name, pwd);
		if( res.isOK() )
			return Result.ok();
		else
			return Result.error( res.error() );
	}
}
