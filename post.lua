trackingIdPrefix = "benchmarking-request-"
my_counter = 0

request = function()
   my_counter = my_counter + 1
   tracking_id = trackingIdPrefix .. my_counter
   wrk.method = "POST"
   wrk.body   = '{"name": "Jochen' .. "-" .. tracking_id ..'"}'
   wrk.headers["Content-Type"] = "application/json"
   wrk.headers["X-ApplicationName"] = "Benchmarking-Test"
   wrk.headers["X-TrackingId"] = tracking_id
   return wrk.format(nil)
end

