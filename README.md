# Building #

# First install Nant (http://nant.sourceforge.net/). Then...


# download repos

git clone git://github.com/bamboo/boo

git clone git://github.com/bamboo/boojay

git clone git://github.com/bamboo/monolipse

git clone git://github.com/bamboo/boo-extensions


# build boo extensions

cd boo-extensions

nant build


# install

cd monolipse

cat > build.properties

	<project name='monolipse properties'>
		<property name='eclipse.dir' value='c:/eclipse' />
	</project>

nant install

