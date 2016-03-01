package com.kepler.collector;

import java.util.ArrayList;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.kepler.annotation.Autowired;
import com.kepler.host.Host;
import com.kepler.management.dependency.Dependency;
import com.kepler.management.dependency.DependencyService;
import com.kepler.management.dependency.Feeder;
import com.kepler.mongo.Dictionary;
import com.kepler.mongo.MongoConfig;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * @author kim 2015年12月30日
 */
@Autowired
public class DependencyHandler implements Feeder {

	private final MongoConfig dependency;

	public DependencyHandler(MongoConfig dependency) {
		super();
		this.dependency = dependency;
	}

	@Override
	// db.dependency.ensureIndex({"host_local_sid": 1})
	public void feed(Host local, Set<Dependency> dependency) {
		this.dependency.collection().update(BasicDBObjectBuilder.start().add(Dictionary.FIELD_HOST_LOCAL_SID, local.sid()).get(), BasicDBObjectBuilder.start("$set", BasicDBObjectBuilder.start().add(Dictionary.FIELD_HOST_LOCAL_SID, local.sid()).add(Dictionary.FIELD_HOST_LOCAL, local.host() + "@" + local.pid()).add(Dictionary.FIELD_HOST_LOCAL_TAG, local.tag()).add(Dictionary.FIELD_HOST_LOCAL_GROUP, local.group()).add(Dictionary.FIELD_PERIOD_MINUTE, TimeUnit.MINUTES.convert(System.currentTimeMillis() + TimeZone.getDefault().getOffset(System.currentTimeMillis()), TimeUnit.MILLISECONDS)).add(Dictionary.FIELD_DEPENDENCY, new Dependencies(dependency)).get()).get(), true, false);
	}

	private class Dependencies extends ArrayList<DBObject> {

		private static final long serialVersionUID = 1L;

		private Dependencies(Set<Dependency> dependency) {
			for (Dependency each : dependency) {
				super.add(BasicDBObjectBuilder.start().add(Dictionary.FIELD_HOST_TARGET_SID, each.host().sid()).add(Dictionary.FIELD_HOST_TARGET, each.host().address()).add(Dictionary.FIELD_HOST_TARGET_GROUP, each.host().group()).add(Dictionary.FIELD_SERVICES, new Services(each.services())).get());
			}
		}
	}

	private class Services extends ArrayList<DBObject> {

		private static final long serialVersionUID = 1L;

		private Services(Set<DependencyService> services) {
			for (DependencyService each : services) {
				super.add(BasicDBObjectBuilder.start().add(Dictionary.FIELD_SERVICE, each.service()).add(Dictionary.FIELD_VERSION, each.versionAndCatalog()).get());
			}
		}
	}
}
