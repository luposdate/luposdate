package lupos.engine.indexconstruction.interfaces;

import java.util.Map;

public interface IParseConfiguration {
	public Map<String, Object> parseConfiguration(String[] args, int argsoffset, final Map<String, Object> configuration);
}
