package br.com.victorandrej.croct.locationdetector.record;

public record Request(String userId, long timeSamp, String ip) {

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Request))
			return false;

		Request reqO = (Request) o;
		return this.userId.equals(reqO.userId) && this.ip.equals(reqO.ip);
	}
}
