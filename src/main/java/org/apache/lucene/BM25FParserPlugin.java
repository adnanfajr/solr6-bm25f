package org.apache.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.lucene.search.BM25FParameters;

public class BM25FParserPlugin extends QParserPlugin {

	public static String NAME = "bm25f";

	private static final Logger logger = LoggerFactory
			.getLogger(BM25FParserPlugin.class);

	BM25FParameters bmParams;

	@Override
	public QParser createParser(String qstr, SolrParams localParams,
			SolrParams params, SolrQueryRequest req) {

		return new BM25FQueryParser(qstr, localParams, params, req, bmParams);
	}

	@SuppressWarnings("unchecked")
	public void init(NamedList args) {

		bmParams = new BM25FParameters();
		if (args == null) {
			return;
		}

		String mainField = (String) args.get("mainField");
		bmParams.setMainField(mainField);

		Object o = args.get("k1");
		if (o == null) {
			logger.warn("cannot find k1 parameter in solr config");
			logger.warn("using default values");
			return;
		}
		float k1 = (Float) o;
		logger.info("K1 = " + k1);
		o = args.get("fieldsBoost");
		if (o == null) {
			logger.warn("cannot find fieldsBoost parameter in solr config");
			logger.warn("using default values");
			return;
		}
		Map<String, String> fieldsBoost = SolrParams.toMap((NamedList) o);
		o = args.get("fieldsB");
		if (o == null) {
			logger.warn("cannot find fieldsB parameter in solr config");
			logger.warn("using default values");
			return;
		}
		Map<String, String> fieldsB = SolrParams.toMap((NamedList) o);
		List<String> f = new ArrayList<String>();
		for (String ffield : fieldsBoost.keySet()) {
			f.add(ffield);
		}

		Collections.sort(f);

		String[] fields = f.toArray(new String[f.size()]);

		Float[] boosts = new Float[fields.length];
		Float[] bParams = new Float[fields.length];

		for (int i = 0; i < fields.length; i++) {
			boosts[i] = Float.parseFloat(fieldsBoost.get(fields[i]));
			bParams[i] = Float.parseFloat(fieldsB.get(fields[i]));
		}

		//bmParams.setFields(fields);

		bmParams.setFieldLengthBoosts(boosts);

		bmParams.setFieldWeights(bParams);
		
		bmParams.setK1(k1);

	}
}