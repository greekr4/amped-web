0 : {
	setSize(1.0,1.0,1.0);
	outline(false);
	setShape("fireball2.shp");
	setBlendType(ADD);
	startAnimation(0,9,0,30,1);
	makeShapeModel("/effect/spark.oa",0,0,0,0,0,1);
	}
400 : {
	colorFadein (0,0,0,0,200);
	}
650 : {
	kill();
	}
