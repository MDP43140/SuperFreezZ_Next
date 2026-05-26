#!/usr/bin/env sh
clear
ops="$1"
[ "$ops" ] || ops="assembleRelease"

# Make sure to use correct Java version (compiler
# really hates incorrect version down to single digit)
for i in \
	/usr/lib/jvm/temurin-26-jdk-amd64 \
	/usr/lib/jvm/temurin-25-jdk-amd64 \
	/usr/lib/jvm/temurin-24-jdk-amd64 \
	/usr/lib/jvm/temurin-23-jdk-amd64 \
	/usr/lib/jvm/temurin-22-jdk-amd64 \
	/usr/lib/jvm/temurin-21-jdk-amd64 \
	/usr/lib/jvm/java-26-openjdk-amd64 \
	/usr/lib/jvm/java-25-openjdk-amd64 \
	/usr/lib/jvm/java-24-openjdk-amd64 \
	/usr/lib/jvm/java-23-openjdk-amd64 \
	/usr/lib/jvm/java-22-openjdk-amd64 \
	/usr/lib/jvm/java-21-openjdk-amd64;do
	if [ -d "$i" ];then
		JAVA_HOME="$i"
		break
	fi
done

# fix files not removed because "is used" causing issues
# where it makes app size 2x bigger and old codes overriden
# newly compiled codes (more prevalent on NTFS partitions)
rm "./*/build/intermediates/dex/release/mergeDexRelease/*"
rm "./*/build/intermediates/dex/release/.fuse_hidden*.tmp"
for subproj in ./*/build;do
	for file in $(ls ${subproj}/intermediates/dex/release/mergeDexRelease);do
		file=$(echo "$file" | sed "s/\*$//")
		mv "${subproj}/intermediates/dex/release/mergeDexRelease/${file}" \
			 "${subproj}/intermediates/dex/release/${file}.tmp";
	done
done

exec nice -n 19 sh gradlew $ops
