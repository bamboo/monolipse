# Building #

###* Install Nant (http://nant.sourceforge.net/)


###* Download repos

git clone git://github.com/bamboo/boo

git clone git://github.com/bamboo/boojay

git clone git://github.com/bamboo/monolipse

git clone git://github.com/bamboo/boo-extensions


###* Build boo extensions

cd boo-extensions

nant build


###* Install

cd monolipse

cat > build.properties

	<project name='monolipse properties'>
		<property name='eclipse.dir' value='c:/eclipse' />
	</project>

nant install

