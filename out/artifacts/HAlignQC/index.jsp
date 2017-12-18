<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <base href="<%=basePath%>">
    <link rel="shortcut icon" href="icon.ico" type="image/x-icon" />
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Halign Server</title>
    <!-- bootstrap -->
    <link href="css/bootstrap-theme.min.css" rel="stylesheet">
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <script src="js/jquery-2.1.1.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <!-- MSA VIEW -->
    <script src="//cdn.bio.sh/msa/latest/msa.min.gz.js"></script>
    
    <style type="text/css">
      body {
              //background-attachment: fixed;
              //background-color: #66512c;
              padding-top: 80px;
            }
      .result {
      	color: blue;
      }
      .warn {
      	color: red;
      }
    </style>
	<script type="text/javascript">
        //Hadoop集群的状态提示
        starting = '<span class="glyphicon glyphicon-forward" aria-hidden="true"></span> Hadoop clusters are starting ... please wait about 40 seconds';
        running = '<span class="glyphicon glyphicon-ok-circle" aria-hidden="true"></span> Hadoop clusters are running ...';
        failed = '<span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span> Hadoop clusters start fail, please contact: shixiangwan@foxmail.com';
        stopped = '<span class="glyphicon glyphicon-remove-circle" aria-hidden="true"></span> Hadoop clusters are stopped, please start first';

    	/* 开启服务器及Hadoop集群 */
    	function start_hadoops() {
    		$('#hadoops_status').css("color", "green");
    		$('#hadoops_status').html(starting);
            change_start_hadoop(0);
    		$.ajax({
                url: "start_hadoops.do",
                type: "GET",
                dataType: "text",
                success: function(data){
                	if (data == "success") {
                		$('#hadoops_status').html(running);
                        change_start_hadoop(0);
                	} else {
                		$('#hadoops_status').css("color", "red");
                		$('#hadoops_status').html(failed);
                        change_start_hadoop(1);
                	}
                },
                error: function(data){
            		$('#hadoops_status').css("color", "red");
            		$('#hadoops_status').html(failed);
                    change_start_hadoop(1);
                }
            }); 
    	}
    	/* 检查服务器及Hadoop集群运行状态 */
    	$(document).ready(function(e) {
            <c:if test="${not empty requestScope.time}">
                $('#presentation1').removeClass("active");
                $('#presentation2').addClass("active");
                $('#home').removeClass("active");
                $('#profile').addClass("active");
            </c:if>
            <c:if test="${not empty requestScope.status}">
                alert("Hadoop clusters are stopped, please start first");
            </c:if>
            $('#check_hadoops').click();
    	});
    	function check_status() {
    		$.ajax({
                url: "check_status.do",
                type: "GET",
                dataType: "text",
                success: function(data){
                	if (data == "running") {
                		$('#hadoops_status').css("color", "green");
                		$('#hadoops_status').html(running);
                        change_start_hadoop(0);
                	} else {
                		$('#hadoops_status').html(stopped);
                        change_start_hadoop(1);
                	}
                },
                error: function(data){ }
            });
    	}
    	function change_start_hadoop(i) {
    	    if (i == 0) {
                $('#start_hadoop').attr("disabled", "disabled");
            } else {
                $('#start_hadoop').removeAttr("disabled");
            }
        }
        function change_alg(i) {
            if (i == 0) {
                //点击蛋白质，禁用tree算法，使用kband
                $('#alg0').attr("disabled", "disabled");
                $('#alg1').attr("disabled", "disabled");
                $('#alg0').removeAttr("checked");
                $('#alg1').removeAttr("checked");
                $('#alg2').prop({checked:true});
            } else {
                //点击DNA，都可以使用
                $('#alg0').removeAttr("disabled");
                $('#alg1').removeAttr("disabled");
                $('#alg0').prop({checked:true});
            }
        }
	</script>
  </head>
  <body>
	<!-- 引入导航栏 -->
    <jsp:include page="header.jsp" />

    <div class="container">
        <div class="row">
            <div class="col-md-4">
                <!--这里放实验部分-->
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h2 class="panel-title">
                            <span class="glyphicon glyphicon-hand-down" aria-hidden="true"></span>
                            Start Now!
                        </h2>
                    </div>
                    <div class="panel-body">
                        <form id="form" action="predict.do" method="post" enctype="multipart/form-data">
                            Step 1.  Upload fasta DNA/RNA/Protein file:
                            <label>
                                <%--<input type="radio" name="name" id="optionsRadios0" value="option1" checked>--%>
                                <a href="download_example.do?type=dna">Download example DNA FASTA file </a><br/>
                                <a href="download_example.do?type=protein">Download example Protein FASTA file </a>
                                <input type="file" name="file" onchange="check(this)">
                            </label>
                            <p>*Note: maximum file size is 50MB.</p>
                            <p class="warn" id="count">${requestScope.error}</p>
                            <p style="height:1px;"></p>

                            Step2. DNA, RNA or Protein ?
                            <div class="radio">
                                <label>
                                    <input type="radio" name="type" id="object1" value="1" onclick="change_alg(1)" checked>
                                    DNA/RNA
                                </label>
                            </div>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="type" id="object2" value="2" onclick="change_alg(0)">
                                    Protein
                                </label>
                            </div>
                            <p style="height:1px;"></p>

                            Step3. Choose an alignment algorithm:
                            <div class="radio">
                                <label>
                                    <input type="radio" name="alg" id="alg0" value="0" checked>
                                    Suffix tree (for DNA/RNA)
                                </label>
                            </div>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="alg" id="alg1" value="1">
                                    Trie tree (for DNA/RNA)
                                </label>
                            </div>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="alg" id="alg2" value="2">
                                    KBand (for DNA/RNA/Protein)
                                </label>
                            </div>
                            <p style="height:1px;"></p>

                            Step4. Choose running mode:
                            <div class="radio" id="hadoop_mode">
                                <label>
                                    <input type="radio" name="mode" id="mode1" value="1" onclick="check_status()" checked>
                                    Hadoop Mode (for large file)
                                </label>
                            </div>
                            <div class="radio">
                                <label>
                                    <input type="radio" name="mode" id="mode2" value="2" onclick="change_start_hadoop(0)">
                                    Standalone Mode (for small file)
                                </label>
                            </div>
                            <p style="height:1px;"></p>

                            <button id="check_hadoops" type="button" onclick="check_status()" style="display:none"></button>
                            <span id="hadoops_status" style="color:orange;"></span>
                            <p style="height:1px;"></p>

                            <button id="start_hadoop" type="button" onclick="start_hadoops()" class="btn btn-primary btn-center" style="width: 100%">
                                <span class="glyphicon glyphicon-off" aria-hidden="true"></span>
                                Start Hadoop
                            </button>
                            <p style="height:1px;"></p>

                            <button type="submit" class="btn btn-primary btn-center" style="width: 100%">
                                <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                                Submit
                            </button>
                        </form>


                    </div>
                </div>
            </div>
            <div class="col-md-8">
                <!-- Nav tabs -->
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="active" id="presentation1"><a href="#home" role="tab" data-toggle="tab">Introduction</a></li>
                    <li role="presentation" id="presentation2"><a href="#profile" role="tab" data-toggle="tab">Align results</a></li>
                </ul>
                <!-- Tab panes -->
                <div class="tab-content">
                    <div role="tabpanel" class="tab-pane active" id="home">
                        <h2 style="color: #28a4c9;">Mutiple Sequences Alignment of DNA/RNA/Protein on Hadoop Clusters</h2>
                        <img src="img/protein.jpg"/>
                        <p style="height:1px;"></p>
                        HAlign is a package of multi-platform Java software tools, which aimed at large scale multiple similar DNA, RNA and Protein sequence alignment. HAlign employs a group of multiple sequence alignment strategies. The input file should be a fasta DNA, RNA or Protein file. You can use this tool in any OS with JVM.
                    </div>
                    <div role="tabpanel" class="tab-pane" id="profile">
                        <p style="height:1px;"></p>
                        <c:if test="${empty requestScope.time}">
                            <p>When hadoop is running, wait for a while patiently, and <span style="color:orange;">do not</span> refresh this page.</p>
                            <p>Thank you for using our service. </p>
                        </c:if>
                        <c:if test="${not empty requestScope.time}">
                            <p>*<strong>Your Job ID:</strong> ${requestScope.time}</p>
                            <p>*<strong>Your Detailed Results:</strong>
                                &nbsp;&nbsp;<strong><a href="download.do?time=${requestScope.time}">Download</a></strong>
                            </p>
                            <p>*<strong>Multiple Sequence Alignment visualization:</strong>
                            <div id="msa">Loading Multiple Alignment...</div>
                            <p>*<strong>YOUR INPUT Sequence visualization:</strong>
                            <div id="msa2">Loading YOUR INPUT...</div>
                        </c:if>
                    </div>
                    <%--引用说明--%>
                    <p style="height:1px;"></p>
                    <div class="alert alert-info">
                        <p><strong>Cite Halign Server in a publication:</strong><br>
                            [1]. Quan Zou, Qinghua Hu, Maozu Guo, Guohua Wang. HAlign: Fast Multiple Similar DNA/RNA Sequence Alignment Based on the Centre Star Strategy. Bioinformatics. 2015,31(15): 2475-2481. (<a target="_bank" href="http://bioinformatics.oxfordjournals.org/cgi/reprint/btv177?ijkey=CbHd7aTXctZ4Ofv&keytype=ref">link</a>)
                            <br>
                            [2]. Quan Zou, Xubin Li, Wenrui Jiang, Ziyu Lin, Guilin Li, Ke Chen. Survey of MapReduce Frame Operation in Bioinformatics. Briefings in Bioinformatics. 2014,15(4): 637-647
                        </p>
                    </div>
                </div>
            </div>
        </div>

    </div> <!-- /container -->

  <script type="text/javascript">
      <c:if test="${not empty requestScope.time}">
          var rootDiv = document.getElementById("msa");
          var opts = {
              el: rootDiv,
              importURL: "./upload/${requestScope.time}.fasta",
          };
          var m = msa(opts);
          var rootDiv2 = document.getElementById("msa2");
          var opts2 = {
              el: rootDiv2,
              importURL: "./upload/input.fasta",
          };
          var m2 = msa(opts2);
      </c:if>
  </script>
  </body>
</html>