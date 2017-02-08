package app.controller;

import app.core.InpackerService;
import app.core.model.Pack;
import app.core.model.PackSettings;
import app.core.model.User;
import app.dto.PackStatusResponse;
import app.dto.MessageResponse;
import app.dto.PackSettingsDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class InpackerController {

    private final InpackerService service;

    @Autowired
    public InpackerController(InpackerService inpackerService) {
        service = inpackerService;
    }

    @RequestMapping(value = "api/user/{username:.+}", method = GET)
    public ResponseEntity<?> getUser(@PathVariable String username) {
        final User user = service.getUser(username);
        if (user == null)
            return status(404).body(new MessageResponse("Not Found"));
        else
            return ok(user);
    }

    @RequestMapping(value = "api/pack/{username:.+}", method = POST)
    public ResponseEntity<Pack> createPack(@PathVariable String username,
                                                         @RequestBody PackSettingsDto packSettingsDto) {
        final PackSettings packSettings = packSettingsDto.getPackSettings();
        final String packName = service.getPackName(username, packSettings);
        final Pack pack = service.getPack(packName);
        if (pack == null) {
            new Thread(() -> service.createPack(username, packSettings)).start();
            return ok(new Pack(packName));
        } else
            return ok(pack);
    }

    @RequestMapping(value = "api/pack/{packName:.+}/status", method = GET)
    public ResponseEntity<Pack> getPackStatus(@PathVariable String packName) {
        return ok(service.getPack(packName));
    }

    @RequestMapping(value = "api/packs", method = GET)
    public ResponseEntity<List<Pack>> getPacksList() {
        return ok(service.getPacks());
    }

    @RequestMapping(value = "packs/{packName:.+}.zip", method = GET, produces = "application/zip")
    @ResponseBody
    public ResponseEntity<FileSystemResource> downloadPack(@PathVariable String packName) {
        final File packFile = service.getPackFile(packName);
        if (packFile == null)
            return status(404).body(null);
        else
            return ok(new FileSystemResource(packFile));
    }
}
