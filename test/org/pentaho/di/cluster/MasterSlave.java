package org.pentaho.di.cluster;

import java.util.Arrays;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.cluster.TransSplitter;

public class MasterSlave extends BaseCluster {

	public void testStartStopSlaveServers() {
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			clusterGenerator.stopSlaveServers();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
	}
	

	/**
	 * This test reads a CSV file in parallel on the master in 1 copy.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 * We want to make sure that only 1 copy is considered.<br>
	 */
	public void testParallelFileReadOnMaster() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = generateParallelFileReadOnMasterTransMeta(clusterGenerator);
			TransExecutionConfiguration config = new TransExecutionConfiguration();
			config.setExecutingClustered(true);
			config.setExecutingLocally(false);
			config.setExecutingRemotely(false);
			config.setClusterPosting(true);
			config.setClusterPreparing(true);
			config.setClusterStarting(true);
			config.setLogLevel(LogWriter.LOG_LEVEL_BASIC);
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation("testParallelFileReadOnMaster", transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-master-result.txt");
			assertEqualsIgnoreWhitespacesAndCase("100", result);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}

	/**
	 * This test reads a CSV file in parallel on the master in 3 copies.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void testParallelFileReadOnMasterWithCopies() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = generateParallelFileReadOnMasterWithCopiesTransMeta(clusterGenerator);
			TransExecutionConfiguration config = new TransExecutionConfiguration();
			config.setExecutingClustered(true);
			config.setExecutingLocally(false);
			config.setExecutingRemotely(false);
			config.setClusterPosting(true);
			config.setClusterPreparing(true);
			config.setClusterStarting(true);
			config.setLogLevel(LogWriter.LOG_LEVEL_BASIC);
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation("testParallelFileReadOnMasterWithCopies", transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-master-result-with-copies.txt");
			assertEqualsIgnoreWhitespacesAndCase("100", result);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}
	

	/**
	 * This test reads a CSV file in parallel on all 3 slaves, each with 1 copy.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void testParallelFileReadOnSlaves() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = generateParallelFileReadOnSlavesTransMeta(clusterGenerator);
			TransExecutionConfiguration config = new TransExecutionConfiguration();
			config.setExecutingClustered(true);
			config.setExecutingLocally(false);
			config.setExecutingRemotely(false);
			config.setClusterPosting(true);
			config.setClusterPreparing(true);
			config.setClusterStarting(true);
			config.setLogLevel(LogWriter.LOG_LEVEL_BASIC);
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation("testParallelFileReadOnSlaves", transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-slaves.txt");
			assertEqualsIgnoreWhitespacesAndCase("100", result);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}
	
	/**
	 * This test reads a CSV file in parallel on all 3 slaves, each with 4 partitions.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void testParallelFileReadOnSlavesWithPartitioning() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = generateParallelFileReadOnSlavesWithPartitioningTransMeta(clusterGenerator);
			TransExecutionConfiguration config = new TransExecutionConfiguration();
			config.setExecutingClustered(true);
			config.setExecutingLocally(false);
			config.setExecutingRemotely(false);
			config.setClusterPosting(true);
			config.setClusterPreparing(true);
			config.setClusterStarting(true);
			config.setLogLevel(LogWriter.LOG_LEVEL_BASIC);
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation("testParallelFileReadOnSlavesWithPartitioning", transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-slaves-with-partitioning.txt");
			assertEqualsIgnoreWhitespacesAndCase("100", result);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}
	
	/**
	 * This test reads a CSV file in parallel on all 3 slaves, each with 4 partitions.<br>
	 * This is a variation on the test right above, with 2 steps in sequence in clustering & partitioning.<br>
	 * It then passes the data over to a dummy step on the slaves.<br>
	 */
	public void testParallelFileReadOnSlavesWithPartitioning2() throws Exception {
		init();
		
		ClusterGenerator clusterGenerator = new ClusterGenerator();
		try {
			clusterGenerator.launchSlaveServers();
			
			TransMeta transMeta = generateParallelFileReadOnSlavesWithPartitioning2TransMeta(clusterGenerator);
			TransExecutionConfiguration config = new TransExecutionConfiguration();
			config.setExecutingClustered(true);
			config.setExecutingLocally(false);
			config.setExecutingRemotely(false);
			config.setClusterPosting(true);
			config.setClusterPreparing(true);
			config.setClusterStarting(true);
			config.setLogLevel(LogWriter.LOG_LEVEL_BASIC);
			TransSplitter transSplitter = Trans.executeClustered(transMeta, config);
			long nrErrors = Trans.monitorClusteredTransformation("testParallelFileReadOnSlavesWithPartitioning2", transSplitter, null, 1);
			assertEquals(0L, nrErrors);
			String result = loadFileContent(transMeta, "${java.io.tmpdir}/test-parallel-file-read-on-slaves-with-partitioning2.txt");
			assertEqualsIgnoreWhitespacesAndCase("100", result);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.toString());
		}
		finally {
			try {
				clusterGenerator.stopSlaveServers();
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.toString());
			}
		}
	}

	private TransMeta generateParallelFileReadOnMasterTransMeta(ClusterGenerator clusterGenerator) throws KettleXMLException {
		TransMeta transMeta = new TransMeta("test/org/pentaho/di/cluster/test-parallel-file-read-on-master.ktr");
		
		// Add the slave servers
		//
		for (SlaveServer slaveServer : ClusterGenerator.LOCAL_TEST_SLAVES) {
			transMeta.getSlaveServers().add(slaveServer);
		}
		
		// Replace the slave servers in the specified cluster schema...
		//
		ClusterSchema clusterSchema = transMeta.findClusterSchema(ClusterGenerator.TEST_CLUSTER_NAME);
		assertNotNull("Cluster schema '"+ClusterGenerator.TEST_CLUSTER_NAME+"' couldn't be found", clusterSchema);
		clusterSchema.getSlaveServers().clear();
		clusterSchema.getSlaveServers().addAll(Arrays.asList(ClusterGenerator.LOCAL_TEST_SLAVES));

		return transMeta;
	}

	private TransMeta generateParallelFileReadOnMasterWithCopiesTransMeta(ClusterGenerator clusterGenerator) throws KettleXMLException {
		TransMeta transMeta = new TransMeta("test/org/pentaho/di/cluster/test-parallel-file-read-on-master-with-copies.ktr");
		
		// Add the slave servers
		//
		for (SlaveServer slaveServer : ClusterGenerator.LOCAL_TEST_SLAVES) {
			transMeta.getSlaveServers().add(slaveServer);
		}
		
		// Replace the slave servers in the specified cluster schema...
		//
		ClusterSchema clusterSchema = transMeta.findClusterSchema(ClusterGenerator.TEST_CLUSTER_NAME);
		assertNotNull("Cluster schema '"+ClusterGenerator.TEST_CLUSTER_NAME+"' couldn't be found", clusterSchema);
		clusterSchema.getSlaveServers().clear();
		clusterSchema.getSlaveServers().addAll(Arrays.asList(ClusterGenerator.LOCAL_TEST_SLAVES));

		return transMeta;
	}
	

	private TransMeta generateParallelFileReadOnSlavesTransMeta(ClusterGenerator clusterGenerator) throws KettleXMLException {
		TransMeta transMeta = new TransMeta("test/org/pentaho/di/cluster/test-parallel-file-read-on-slaves.ktr");
		
		// Add the slave servers
		//
		for (SlaveServer slaveServer : ClusterGenerator.LOCAL_TEST_SLAVES) {
			transMeta.getSlaveServers().add(slaveServer);
		}
		
		// Replace the slave servers in the specified cluster schema...
		//
		ClusterSchema clusterSchema = transMeta.findClusterSchema(ClusterGenerator.TEST_CLUSTER_NAME);
		assertNotNull("Cluster schema '"+ClusterGenerator.TEST_CLUSTER_NAME+"' couldn't be found", clusterSchema);
		clusterSchema.getSlaveServers().clear();
		clusterSchema.getSlaveServers().addAll(Arrays.asList(ClusterGenerator.LOCAL_TEST_SLAVES));

		return transMeta;
	}
	
	

	private TransMeta generateParallelFileReadOnSlavesWithPartitioningTransMeta(ClusterGenerator clusterGenerator) throws KettleXMLException {
		TransMeta transMeta = new TransMeta("test/org/pentaho/di/cluster/test-parallel-file-read-on-slaves-with-partitioning.ktr");
		
		// Add the slave servers
		//
		for (SlaveServer slaveServer : ClusterGenerator.LOCAL_TEST_SLAVES) {
			transMeta.getSlaveServers().add(slaveServer);
		}
		
		// Replace the slave servers in the specified cluster schema...
		//
		ClusterSchema clusterSchema = transMeta.findClusterSchema(ClusterGenerator.TEST_CLUSTER_NAME);
		assertNotNull("Cluster schema '"+ClusterGenerator.TEST_CLUSTER_NAME+"' couldn't be found", clusterSchema);
		clusterSchema.getSlaveServers().clear();
		clusterSchema.getSlaveServers().addAll(Arrays.asList(ClusterGenerator.LOCAL_TEST_SLAVES));

		return transMeta;
	}
	
	

	private TransMeta generateParallelFileReadOnSlavesWithPartitioning2TransMeta(ClusterGenerator clusterGenerator) throws KettleXMLException {
		TransMeta transMeta = new TransMeta("test/org/pentaho/di/cluster/test-parallel-file-read-on-slaves-with-partitioning2.ktr");
		
		// Add the slave servers
		//
		for (SlaveServer slaveServer : ClusterGenerator.LOCAL_TEST_SLAVES) {
			transMeta.getSlaveServers().add(slaveServer);
		}
		
		// Replace the slave servers in the specified cluster schema...
		//
		ClusterSchema clusterSchema = transMeta.findClusterSchema(ClusterGenerator.TEST_CLUSTER_NAME);
		assertNotNull("Cluster schema '"+ClusterGenerator.TEST_CLUSTER_NAME+"' couldn't be found", clusterSchema);
		clusterSchema.getSlaveServers().clear();
		clusterSchema.getSlaveServers().addAll(Arrays.asList(ClusterGenerator.LOCAL_TEST_SLAVES));

		return transMeta;
	}
}