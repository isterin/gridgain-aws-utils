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
 * Created by IntelliJ IDEA.
 * User: ilya
 * Date: 3/1/12
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */

class GridTcpDiscoveryDynamoDbIpFinder(val awsCredentials: AWSCredentials, val tableName: String, val primaryKeyName:String = "Id")
  extends GridTcpDiscoveryIpFinderAdapter {

  setShared(true)

  private lazy val client = new AmazonDynamoDBClient(awsCredentials)

  def getRegisteredAddresses = {
    val res = client.scan(new ScanRequest(tableName))
    for (item <- res.getItems)
      yield new InetSocketAddress(InetAddress.getByName(item("node_id").getS), item("port").getN.toInt)
  }

  def registerAddresses(nodes: Collection[InetSocketAddress]) {
    nodes.foreach(n => {
      println("Registering... " + n)
      val item = Map[String, AttributeValue](
        "node_id" -> new AttributeValue().withS(n.getAddress.getHostAddress),
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

  def unregisterAddresses(nodes: Collection[InetSocketAddress]) {
    nodes.foreach(n => {
      println("Unregistering... " + n)
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
