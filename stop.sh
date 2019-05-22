#!/bin/sh

ps -ef |grep symphony-connection |grep -v grep |awk '{print $2}'|xargs kill -9
