#/bin/bash
# host to monitor
# Example remote_host=192.168.16.210
#remote_host=192.168.19.10
remote_host=
if [ -z $remote_host ]; then
	echo "remote_host is not configured"
	exit
fi

# Runs loadtests in soapUI
# This path needs to be set.
# Example /Applications/soapui/soapUI-4.5.2.app/
#soapUIpath="/Applications/soapui/eviware/soapUI-3.0.1.app/"
soapUIpath=
if [ -z "$soapUIpath" ]; then
	echo "The path to SoapUI needs to be configured!"
	exit
fi

soapUIpath=$soapUIpath"Contents/Resources/app/bin/loadtestrunner.sh"
if [ ! -f "$soapUIpath" ]; then
	echo "Invalid path for SoapUI!"
	echo $soapUIpath
	exit
fi

# Path to SoapUI test file
# Example:  /Users/patrik/callista/ei/svnei6/trunk/modules/intsvc/tests/soapui/SKLTP-EI-loadtests-soapui-project.xml 
#testFilePath="/Users/patrik/callista/ei/svnei6/trunk/modules/intsvc/tests/soapui/SKLTP-EI-loadtests-soapui-project.xml" 
testFilePath= 
if [ ! -f "$testFilePath" ]; then
	echo "Invalid path for test file!"
	echo $testFilePath
	exit
fi


filepath=/tmp/ei_loadtests
mkdir $filepath
rm -r $filepath/*.*

echo "Starts cpu profiler"
./cpu.sh "$filepath/timing.log" "$remote_host" &
thepid=$!

echo "Calling loadrunner.sh"
$soapUIpath -r $testFilePath -f $filepath
#$soapUIpath -r ~/callista/ei/ei/modules/intsvc/SKLTP-EI-loadtests-soapui-project.xml -f $filepath
#$soapUIpath -r -f $filepath -l "FindContent 2 mandatory - 10 thread" $testFilePath
sleep 10
echo "Killing the cpu profiler with pid ${thepid}"
kill $thepid


# Remove timing errors
echo "Removing debug infomration"
find $filepath -iname "*error*entry.txt" -d 1 -exec rm {} \;

# Move soapui.log and family
echo "Moving soapui.log"
mv *.log* $filepath/


echo "Calculating stats"
ruby stats.rb $filepath
