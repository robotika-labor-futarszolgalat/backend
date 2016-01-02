package hu.elte.ik.robotika.futar.vertx.backend.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class User {

	private static final AtomicInteger COUNTER = new AtomicInteger();

	private final int id;
	private String name;
	private String room;

	public User(String name, String room) {
		this.id = COUNTER.getAndIncrement();
		this.name = name;
		this.room = room;
	}

	public User() {
		this.id = COUNTER.getAndIncrement();
	}

	public String getName() {
		return name;
	}

	public String getRoom() {
		return room;
	}

	public int getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRoom(String room) {
		this.room = room;
	}
}
