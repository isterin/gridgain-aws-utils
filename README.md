Using with SBT...

    resolvers += "isterin.github" at "https://raw.github.com/isterin/maven-repo/master/snapshots/"

    libraryDependencies += "com.github.isterin" %% "gridgain-aws-utils" % "0.1-SNAPSHOT"


Usage for GridTcpDiscoveryDynamoDbIpFinder:

    <property name="discoverySpi">
        <bean class="org.gridgain.grid.spi.discovery.tcp.GridTcpDiscoverySpi">
            <property name="localAddress" value="#{ T(java.net.InetAddress).getLocalHost().getHostAddress() }"/>
            <property name="ipFinder">
                <bean class="com.github.isterin.gridgain.spi.discovery.GridTcpDiscoveryDynamoDbIpFinder">
                    <constructor-arg>
                        <bean class="com.amazonaws.auth.BasicAWSCredentials">
                            <constructor-arg value="--your aws key---"/>
                            <constructor-arg value="--your aws private key--"/>
                        </bean>
                    </constructor-arg>
                    <constructor-arg value="gg-table-name"/>
                    <constructor-arg value="node_id"/>
                </bean>
            </property>
            <property name="heartbeatFrequency" value="3000"/>
        </bean>
    </property>

