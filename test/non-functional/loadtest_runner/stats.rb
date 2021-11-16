require "time"

Dir.chdir(ARGV.first) unless ARGV.first.nil?

best_tps = {}
lines = []

files = Dir.glob("*statistics.txt")
files.each do |f| 
    
  test = f.split("__").first
  matches = f.match(/((\d+)[-_]threads?)/)

  next if matches.nil?
  
  threads = matches[2]
  threads_str = matches[0]

  line = File.readlines(f).last
  min = line.split(",")[1]
  max = line.split(",")[2]
  avg = line.split(",")[3]
  tps = line.split(",")[6].to_f
  err = line.split(",")[9]


  puts "#{test} - #{threads} thread(s)"
  puts "avg: #{avg}"
  puts "min: #{min}"
  puts "max: #{max}"
  puts "tps: #{tps}"
  puts ""

  if tps > best_tps[test].to_f
    best_tps[test] = tps
  end


  # Find starting time
  starting_time = File.new("soapui.log").readlines.select{|x| x.include?("Running LoadTest")}.first
  starting_time = Time.parse(starting_time.split(" ")[0,2].join(" ")) #timepart

  # Find ending time
  ending_time = File.new("soapui.log").readlines.select{|x| x.include?("Exported") &&  x.include?(f) }.first
  
  if ending_time.nil?
    puts "Could not parse ending_time for #{test}. Bad input data. Trying next test"
    next
  end

  ending_time = Time.parse(ending_time.split(" ")[0,2].join(" ")) #timepart


  loads = []
  cpus = []

  # Parse stats file
  File.new("timing.log").readlines.each do |timing_line|
    t = Time.parse(timing_line.split(",").first)
    if t.between?(starting_time, ending_time)
      _,c,l = timing_line.split(";")
      l.sub!(",","") # remove trailing comma
      cpus << (c.to_f)
      loads << (l.to_f)
    end
  end



  avg_load = "NA"
  avg_cpu = "NA"

  puts "Loads-size: #{loads.size}"
  puts "cpu-size: #{cpus.size}"

  if loads.size > 0
    avg_load = '%.2f' % (loads.inject(:+) / loads.size)
  end
  if cpus.size > 0
    avg_cpu = '%.2f' % (cpus.inject(:+) / cpus.size)
  end
 
  #testname,threads,tps,avg,min,max,err,avg_load,avg_cpu
  line = "#{test},#{threads},#{tps},#{avg},#{min},#{max},#{err},#{avg_load},#{avg_cpu}"
  lines << line
end



puts "Best tps per test"
best_tps.each do |key, value|
  puts "#{key}: #{value} tps"
end

puts ""
lines.sort!
lines.unshift("testname,threads,tps,avg,min,max,err,avg_load,avg_cpu")
lines.each do |line|
  puts line
end


puts ""
lines.unshift("testname,tps,avg")
lines.each do |line|
  test, _, tps, avg= line.split(",")[0,4]
  puts "#{test},#{tps},#{avg}"
end

