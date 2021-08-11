package dao;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="SIMPLE_TABLE")
public class SimpleEntity {
	
	@Id 
	@Column(name="ident")
	private long identity;
	
	@Column(name="time_stamp")
	private Timestamp timeStamp;
	
	@Column(name="test_string")
	private String testNvcarhcar;

	public long getIdentity() {
		return identity;
	}

	public void setIdentity(long identity) {
		this.identity = identity;
	}

	public Timestamp getTimeStamp() {
	    return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
	    this.timeStamp = timeStamp;
	}

	public String getTestNvcarhcar() {
		return testNvcarhcar;
	}

	public void setTestNvcarhcar(String testNvcarhcar) {
		this.testNvcarhcar = testNvcarhcar;
	}

	public SimpleEntity() {
		super();
	}

	@Override
	public String toString() {
		return "SimpleEntity [identity=" + identity + ", timeStamp=" + timeStamp + ", testNvcarhcar=" + testNvcarhcar
				+ "]";
	}
	
	
	
}
