package site.iris.issuefy.eums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DashBoardRank {
	S(80, "S"),
	A(60, "A"),
	B(40, "B"),
	C(20, "C"),
	D(0, "D");

	private final int threshold;
	private final String label;

	public static String getRankLabel(int score) {
		for (DashBoardRank rank : values()) {
			if (score >= rank.threshold) {
				return rank.label;
			}
		}
		return D.label;
	}
}
