/*
 * Copyright (C) Ilya Sterin under GPLv3, http://www.gnu.org/licenses/gpl.html
 */
package com.github.isterin.gridgain.spi.discovery

import org.gridgain.grid.spi.discovery.tcp.ipfinder.GridTcpDiscoveryIpFinderAdapter
import java.util.Collection
import scala.collection.JavaConversions._
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient
import java.net.{InetAddress, InetSocketAddress}
import com.amazonaws.services.dynamodb.model._
import org.gridgain.grid.spi.GridSpiException

/**
 * Author: Ilya Sterin
 * Date: 3/1/12
 * Time: 2:38 PM
 *
 * Allows discovery of Gridgain nodes using Amazon's DynamoDB.  You can specify your AWS credentials, table name,
 * and the primary key to use.
 *
 * <property name="discoverySpi">
 *     <bean class="org.gridgain.grid.spi.discovery.tcp.GridTcpDiscoverySpi">
 *         <property name="localAddress" value="#{ T(java.net.InetAddress).getLocalHost().getHostAddress() }"/>
 *         <property name="ipFinder">
 *             <bean class="com.github.isterin.gridgain.spi.discovery.GridTcpDiscoveryDynamoDbIpFinder">
 *                 <constructor-arg>
 *                     <bean class="com.amazonaws.auth.BasicAWSCredentials">
 *                         <constructor-arg value="--your aws key---"/>
 *                         <constructor-arg value="--your aws private key--"/>
 *                     </bean>
 *                 </constructor-arg>
 *                 <constructor-arg value="gg-table-name"/>
 *                 <constructor-arg value="node_id"/>
 *             </bean>
 *         </property>
 *         <property name="heartbeatFrequency" value="3000"/>
 *     </bean>
 * </property>
 *
 *
 * @param awsCredentials
 * @param tableName This table must be created before.  ** The table will not be created if it doesn't exist **
 * @param primaryKeyName specified when you created the DynamoDB table [defaults to "Id"]
 */
class GridTcpDiscoveryDynamoDbIpFinder(val awsCredentials: AWSCredentials, val tableName: String, val primaryKeyName: String = "Id")
  extends GridTcpDiscoveryIpFinderAdapter {

  setShared(true)

  private lazy val client = new AmazonDynamoDBClient(awsCredentials)

  /**
   * Retrieves and returns all addresses registered in DynamoDB table specified.
   */
  def getRegisteredAddresses = {
    val res = client.scan(new ScanRequest(tableName))
    for (item <- res.getItems)
    yield new InetSocketAddress(InetAddress.getByName(item(primaryKeyName).getS), item("port").getN.toInt)
  }

  /**
   * Stores addresses in DynamoDB.
   *
   * @param nodes collection of addresses to store in DynamoDB
   */
  def registerAddresses(nodes: Collection[InetSocketAddress]) {
    nodes.foreach(n => {
      val item = Map[String, AttributeValue](
        primaryKeyName -> new AttributeValue().withS(n.getAddress.getHostAddress),
        "port" -> new AttributeValue().withN(n.getPort.toString)
      )
      try {
        client.putItem(new PutItemRequest(tableName, item))
      }
      catch {
        case e => throw new GridSpiException("Failed to add address [" + n.getHostName + ":" + n.getPort + "] to [" + tableName + "]", e);
      }
    })
  }

  /**
   * Removes addresses from DynamoDB.
   *
   * @param nodes collection of addresses to store in DynamoDB
   */
  def unregisterAddresses(nodes: Collection[InetSocketAddress]) {
    nodes.foreach(n => {
      try {
        client.deleteItem(
          new DeleteItemRequest(tableName, new Key(new AttributeValue(n.getAddress.getHostAddress))))
      }
      catch {
        case e => throw new GridSpiException("Failed to remove address [" + n.getHostName + ":" + n.getPort + "] to [" + tableName + "]", e);
      }
    })
  }
}
