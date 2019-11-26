#!/bin/bash

cd `dirname $0`
cd ..
HOME=`pwd`

export DAVINCI3_HOME=$HOME


if [ -z "$DAVINCI3_HOME" ]; then
  echo "DAVINCI3_HOME not found"
  echo "Please export DAVINCI3_HOME to your environment variable"
  exit
fi

cd $DAVINCI3_HOME
Lib_dir=`ls | grep lib`
if [ -z "$Lib_dir" ]; then
  echo "Invalid DAVINCI3_HOME"
  exit
fi

export DWS_ENGINE_ANAGER_PID=$HOME/bin/visualis-server.pid

if [[ -f "${DWS_ENGINE_ANAGER_PID}" ]]; then
    pid=$(cat ${DWS_ENGINE_ANAGER_PID})
    if kill -0 ${pid} >/dev/null 2>&1; then
      echo "Vangogh is already running."
      return 0;
    fi
fi
export DWS_ENGINE_ANAGER_LOG_PATH=$HOME/logs
export DWS_ENGINE_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=51206"
export DWS_ENGINE_ANAGER_HEAP_SIZE="4G"
export DWS_ENGINE_ANAGER_JAVA_OPTS="-Xms$DWS_ENGINE_ANAGER_HEAP_SIZE -Xmx$DWS_ENGINE_ANAGER_HEAP_SIZE -XX:+UseG1GC -XX:MaxPermSize=500m $DWS_ENGINE_DEBUG"

nohup java $DWS_ENGINE_ANAGER_JAVA_OPTS -cp $HOME/conf:$HOME/lib/*:$JAVA_HOME/lib/* com.webank.wedatasphere.linkis.DataWorkCloudApplication 2>&1 > $DWS_ENGINE_ANAGER_LOG_PATH/linkis.out &
pid=$!
if [[ -z "${pid}" ]]; then
    echo "visualis-server start failed!"
    exit 1
else
    echo "visualis-server start succeeded!"
    echo $pid > $DWS_ENGINE_ANAGER_PID
    sleep 1
fi

